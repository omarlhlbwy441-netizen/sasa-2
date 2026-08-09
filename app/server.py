import os
import json
import http.server
import socketserver

PORT = int(os.environ.get("PORT", 10000))
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DIRECTORY = os.path.join(BASE_DIR, "www")

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def do_GET(self):
        if self.path == "/api/config":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            api_key = os.environ.get("GEMINI_API_KEY", "")
            self.wfile.write(json.dumps({"apiKey": api_key}).encode("utf-8"))
            return
        return super().do_GET()

    def guess_type(self, path):
        if path.endswith(".apk"):
            return "application/vnd.android.package-archive"
        return super().guess_type(path)

print(f"Starting server on port {PORT} serving {DIRECTORY}...")
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.serve_forever()
