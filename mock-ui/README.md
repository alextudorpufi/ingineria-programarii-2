<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

## Run Locally

**Prerequisites:** Node.js 20+ and npm


1. Install Node.js and npm if they are not already installed. On macOS with Homebrew:
   `brew install node`

   You can also install Node.js from [nodejs.org](https://nodejs.org/).
2. Verify that Node.js and npm are available:
   `node --version`
   `npm --version`
3. Install the project dependencies from this directory:
   `npm install`
4. Set the `GEMINI_API_KEY` in [.env.local](.env.local) to your Gemini API key.
5. Run the app:
   `npm run dev`

If you see `vite: command not found`, run `npm install` in `mock-ui` and then retry
`npm run dev`.
