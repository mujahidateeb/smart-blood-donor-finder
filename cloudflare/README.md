# Cloudflare Tunnel Setup for Smart Blood Donor Finder

This folder contains configuration templates and assets for exposing the application via Cloudflare Tunnel with a static URL.

## Files
- `config.example.yml`: Template configuration for a locally-managed named tunnel.
- Copy `config.example.yml` to `config.yml` when configuring a local tunnel.

## Recommended Option (Zero Trust Dashboard Token)
For the simplest static URL without managing config files:
1. Create a tunnel in the Cloudflare Zero Trust Dashboard (`one.dash.cloudflare.com` -> Networks -> Tunnels).
2. Configure public hostname pointing to `http://localhost:8080`.
3. Add the token to `.env` as `CLOUDFLARE_TUNNEL_TOKEN=ey...`.
4. Run `start-tunnel.bat` in the root project folder.

See `CLOUDFLARE_SETUP.md` in the project root for full step-by-step instructions.
