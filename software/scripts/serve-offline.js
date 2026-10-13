const http = require("http");
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const [portText, apiPortText] = process.argv.slice(2);
const port = Number(portText);
const apiPort = Number(apiPortText);
const frontendDist = path.resolve(root, "Smart-Enokimushroom-front", "dist");
const apiHost = "127.0.0.1";

const mimeTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".js": "application/javascript; charset=utf-8",
  ".mjs": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".map": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".gif": "image/gif",
  ".webp": "image/webp",
  ".svg": "image/svg+xml",
  ".ttf": "font/ttf",
  ".otf": "font/otf",
  ".woff": "font/woff",
  ".woff2": "font/woff2",
  ".eot": "application/vnd.ms-fontobject"
};

// Requests whose path starts with one of these prefixes are proxied to the API.
// The /backend prefix is stripped; other prefixes are forwarded as-is.
const API_PREFIXES = [
  "/backend",
  "/api/",
  "/captcha",
  "/common/",
  "/profile/",
  "/system/",
  "/monitor/",
  "/tool/",
  "/druid/",
  "/login",
  "/register",
  "/getInfo",
  "/logout",
  "/chat"
];

function shouldProxy(requestPath) {
  return API_PREFIXES.some(prefix => requestPath === prefix || requestPath.startsWith(prefix + "/") || requestPath.startsWith(prefix + "?"));
}

function proxy(request, response, stripPrefix) {
  const upstreamPath = stripPrefix ? request.url.replace(/^\/backend/, "") : request.url;
  const upstream = http.request(
    {
      host: apiHost,
      port: apiPort,
      path: upstreamPath || "/",
      method: request.method,
      headers: request.headers
    },
    upstreamResponse => {
      response.writeHead(upstreamResponse.statusCode, upstreamResponse.headers);
      upstreamResponse.pipe(response);
    }
  );
  upstream.on("error", error => {
    response.writeHead(502, { "Content-Type": "text/plain; charset=utf-8" });
    response.end(`API unavailable: ${error.message}`);
  });
  request.pipe(upstream);
}

function injectRuntimeConfig(html) {
  const script = `<script>window.__ENOKI_API_BASE__=location.protocol+'//'+location.host+'/backend';</script>`;
  const headEnd = html.toLowerCase().indexOf("</head>");
  if (headEnd >= 0) {
    return html.slice(0, headEnd) + script + html.slice(headEnd);
  }
  return html + script;
}

function serveStatic(request, response) {
  const requestPath = decodeURIComponent(request.url.split("?")[0]);

  if (shouldProxy(requestPath)) {
    const strip = requestPath === "/backend" || requestPath.startsWith("/backend/");
    return proxy(request, response, strip);
  }

  const filePath = path.resolve(frontendDist, "." + requestPath);
  if (!filePath.startsWith(frontendDist)) {
    return response.writeHead(403).end();
  }

  const isFile = fs.existsSync(filePath) && fs.statSync(filePath).isFile();
  const target = isFile ? filePath : path.join(frontendDist, "index.html");

  fs.readFile(target, (error, data) => {
    if (error) return response.writeHead(404).end("Not found");
    const contentType = mimeTypes[path.extname(target)] || "application/octet-stream";
    let body = data;
    if (target.endsWith("index.html")) {
      body = Buffer.from(injectRuntimeConfig(data.toString("utf8")), "utf8");
    }
    response.writeHead(200, {
      "Content-Type": contentType,
      "Cache-Control": target.endsWith("index.html") ? "no-cache" : "public, max-age=86400"
    });
    response.end(body);
  });
}

function start() {
  const server = http.createServer((request, response) => {
    serveStatic(request, response);
  });
  server.listen(port, "0.0.0.0", () => {
    console.log(`Frontend listening on http://localhost:${port} (API -> :${apiPort})`);
  });
}

if (
  !Number.isInteger(port) ||
  port < 1 ||
  port > 65535 ||
  !Number.isInteger(apiPort) ||
  apiPort < 1 ||
  apiPort > 65535
) {
  console.error("Usage: node scripts/serve-offline.js <port> <api-port>");
  process.exit(1);
}

start();
