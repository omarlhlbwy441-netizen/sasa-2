import os
import json
import urllib.request
import urllib.error
import http.server
import socketserver

PORT = int(os.environ.get("PORT", 10000))
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DIRECTORY = os.path.join(BASE_DIR, "www")

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
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
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
        if self.path.startswith("/api/chat"):
            content_length = int(self.headers.get("Content-Length", 0))
            post_data = self.rfile.read(content_length)
            
            try:
                body = json.loads(post_data.decode("utf-8"))
                model = body.get("model", "gemini-3.6-flash")
                contents = body.get("contents", [])
                
                api_key = get_gemini_key()
                if not api_key:
                    self.send_response(400)
                    self.send_header("Content-Type", "application/json")
                    self.end_headers()
                    self.wfile.write(json.dumps({"error": {"message": "لم يتم العثور على GEMINI_API_KEY في متغيرات بيئة Render!"}}).encode("utf-8"))
                    return

                target_url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
                req = urllib.request.Request(
                    target_url,
                    data=json.dumps({"contents": contents}).encode("utf-8"),
                    headers={"Content-Type": "application/json"},
                    method="POST"
                )

                with urllib.request.urlopen(req) as resp:
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
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(json.dumps({"error": {"message": str(e)}}).encode("utf-8"))
            return

        self.send_response(404)
        self.end_headers()

    def guess_type(self, path):
        if path.endswith(".apk"):
            return "application/vnd.android.package-archive"
        return super().guess_type(path)

print(f"Starting server on port {PORT} serving {DIRECTORY}...")
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.serve_forever()
