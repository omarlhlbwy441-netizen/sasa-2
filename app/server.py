import os
import http.server
import socketserver

PORT = int(os.environ.get("PORT", 10000))
DIRECTORY = "/app/www"

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def guess_type(self, path):
        if path.endswith(".apk"):
            return "application/vnd.android.package-archive"
        return super().guess_type(path)

print(f"Starting server on port {PORT} serving {DIRECTORY}...")
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.serve_forever()
