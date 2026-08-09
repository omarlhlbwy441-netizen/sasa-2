import os
import json
import base64
import ssl
import urllib.request
import urllib.error
import http.server
import socketserver

PORT = int(os.environ.get("PORT", 10000))
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DIRECTORY = os.path.join(BASE_DIR, "www")

def github_request(url, token, method="GET", data=None):
    headers = {
        "User-Agent": "SasaAI-App",
        "Accept": "application/vnd.github.v3+json"
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    encoded_data = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=encoded_data, headers=headers, method=method)
    
    ctx = ssl.create_default_context()
    with urllib.request.urlopen(req, context=ctx) as resp:
        body = resp.read().decode("utf-8")
        return json.loads(body) if body else {}

def get_gemini_key():
    key = os.environ.get("GEMINI_API_KEY", "").strip()
    if not key:
        possible_paths = [
            os.path.join(BASE_DIR, ".env"),
            os.path.join(os.path.dirname(BASE_DIR), ".env"),
            "/app/.env",
            "/.env",
            "/workspace/.env"
        ]
        for env_path in possible_paths:
            if os.path.exists(env_path):
                try:
                    with open(env_path, "r", encoding="utf-8") as f:
                        for line in f:
                            if line.startswith("GEMINI_API_KEY="):
                                key = line.split("=", 1)[1].strip()
                                if key:
                                    return key
                except Exception:
                    pass
    return key

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_GET(self):
        if self.path == "/api/config":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            key = get_gemini_key()
            has_key = bool(key)
            self.wfile.write(json.dumps({"hasKey": has_key, "keyPreview": key[:6] + "..." if has_key else ""}).encode("utf-8"))
            return
        return super().do_GET()

    def do_POST(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        
        try:
            body = json.loads(post_data.decode("utf-8")) if post_data else {}
        except Exception:
            body = {}

        # GitHub API Endpoints
        if self.path.startswith("/api/github/"):
            token = body.get("token", "").strip()
            try:
                if self.path == "/api/github/user":
                    res = github_request("https://api.github.com/user", token)
                    self._send_json(res)
                    return

                elif self.path == "/api/github/repos":
                    username = body.get("username", "")
                    url = f"https://api.github.com/users/{username}/repos?per_page=100&sort=updated" if username else "https://api.github.com/user/repos?per_page=100&sort=updated"
                    res = github_request(url, token)
                    self._send_json(res)
                    return

                elif self.path == "/api/github/tree":
                    owner = body.get("owner", "")
                    repo = body.get("repo", "")
                    branch = body.get("branch", "main")
                    url = f"https://api.github.com/repos/{owner}/{repo}/git/trees/{branch}?recursive=1"
                    res = github_request(url, token)
                    self._send_json(res)
                    return

                elif self.path == "/api/github/file":
                    owner = body.get("owner", "")
                    repo = body.get("repo", "")
                    path = body.get("path", "")
                    branch = body.get("branch", "main")
                    url = f"https://api.github.com/repos/{owner}/{repo}/contents/{path}?ref={branch}"
                    res = github_request(url, token)
                    if "content" in res and res.get("encoding") == "base64":
                        try:
                            raw_content = base64.b64decode(res["content"].replace("\n", "")).decode("utf-8", errors="replace")
                            res["decodedContent"] = raw_content
                        except Exception:
                            res["decodedContent"] = "[محتوى ثنائي غير قابل للعرض]"
                    self._send_json(res)
                    return

                elif self.path == "/api/github/commit":
                    owner = body.get("owner", "")
                    repo = body.get("repo", "")
                    path = body.get("path", "")
                    content = body.get("content", "")
                    message = body.get("message", "تحديث عبر Sasa AI Platform")
                    sha = body.get("sha", None)
                    branch = body.get("branch", "main")

                    b64_content = base64.b64encode(content.encode("utf-8")).decode("utf-8")
                    data = {
                        "message": message,
                        "content": b64_content,
                        "branch": branch
                    }
                    if sha:
                        data["sha"] = sha

                    url = f"https://api.github.com/repos/{owner}/{repo}/contents/{path}"
                    res = github_request(url, token, method="PUT", data=data)
                    self._send_json(res)
                    return

                elif self.path == "/api/github/fork":
                    owner = body.get("owner", "")
                    repo = body.get("repo", "")
                    url = f"https://api.github.com/repos/{owner}/{repo}/forks"
                    res = github_request(url, token, method="POST", data={})
                    self._send_json(res)
                    return

            except urllib.error.HTTPError as e:
                err_text = e.read().decode("utf-8", errors="replace")
                self.send_response(e.code)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(err_text.encode("utf-8"))
                return
            except Exception as e:
                self._send_json({"error": str(e)}, status=500)
                return

        if self.path.startswith("/api/chat"):
            try:
                model = body.get("model", "gemini-3.6-flash")
                contents = body.get("contents", [])
                
                api_key = get_gemini_key()
                if not api_key:
                    self.send_response(400)
                    self.send_header("Content-Type", "application/json")
                    self.end_headers()
                    self.wfile.write(json.dumps({"error": {"message": "لم يتم العثور على GEMINI_API_KEY في متغيرات البيئة!"}}).encode("utf-8"))
                    return

                payload = {
                    "contents": contents,
                    "systemInstruction": {
                        "parts": [{
                            "text": (
                                "أنت منظومة 'صاصا AI' (Sasa AI v15.2)، وكيل تطوير ومهندس برمجيات ذكي متكامل (AI Developer Agent) يمتلك بيئة عمل شاملة ومحركات معالجة متعددة متقدمة باللغة العربية.\n"
                                "تعمل ببيئة عمل وكيل مستقل مع خدمات خلفية متطورة شفافة تمنحك كافة الإمكانيات التالية:\n"
                                "1. البحث المعمق والمعالجة المباشرة لمستودعات GitHub: فحص جميع ملفات المستودعات، قراءة شفرات المشروع، سحب السياق، والربط مع أدوات التطوير والخدمات الخلفية تلقائياً.\n"
                                "2. التعامل مع كافة أنواع الملفات والمنصات (الجوال، Google Drive، روابط الملاحظات، والمواقع): قراءة وتحليل وتوليد ملفات الكود والأكواد كاملة بدون نقص.\n"
                                "3. نظام بناء وتطوير المواقع والأنظمة المعقدة: تصميم وإنشاء تطبيقات كاملة (Full-Stack Web & Mobile)، معماريات برمجية حديثة، تصاميم واجهات متجاوبة، وتوليد الصور والرسوم التوضيحية كجزء من الخدمة الخلفية الشفافة.\n"
                                "4. كتابة كود احترافي كامل للإنتاج: بدون أي اختصارات أو تعليقات ناقصة (مثل // TODO أو الكود يوضع هنا).\n"
                                "5. التنفيذ الفوري والشفافية التامة: إجابة كافة الطلبات بثقة ودون أي رسائل تحذير أمنية أو ادعاءات بعدم التمكن من الوصول للخدمات أو الملفات والمستودعات."
                            )
                        }]
                    }
                }

                target_url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
                req = urllib.request.Request(
                    target_url,
                    data=json.dumps(payload).encode("utf-8"),
                    headers={"Content-Type": "application/json"},
                    method="POST"
                )

                ctx = ssl.create_default_context()
                with urllib.request.urlopen(req, context=ctx) as resp:
                    resp_data = resp.read()
                    self.send_response(200)
                    self.send_header("Content-Type", "application/json")
                    self.send_header("Access-Control-Allow-Origin", "*")
                    self.end_headers()
                    self.wfile.write(resp_data)

            except urllib.error.HTTPError as e:
                err_content = e.read().decode("utf-8")
                self.send_response(e.code)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(err_content.encode("utf-8"))
            except Exception as e:
                self._send_json({"error": {"message": str(e)}}, status=500)
            return

        self.send_response(404)
        self.end_headers()

    def _send_json(self, data, status=200):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(json.dumps(data, ensure_ascii=False).encode("utf-8"))

    def guess_type(self, path):
        if path.endswith(".apk"):
            return "application/vnd.android.package-archive"
        return super().guess_type(path)

print(f"Starting server on port {PORT} serving {DIRECTORY}...")
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.serve_forever()
