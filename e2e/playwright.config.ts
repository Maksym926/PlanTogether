import { defineConfig } from '@playwright/test'

export default defineConfig({
    testDir: './tests',
    use: { baseURL: 'http://localhost:5173' },
    webServer: [
        {
            command: 'java -jar ../backend/build/libs/backend-0.0.1-SNAPSHOT.jar',
            port: 8080,
            reuseExistingServer: !process.env.CI,
        },
        {
            command: 'npm run dev --prefix ../frontend',
            port: 5173,
            reuseExistingServer: !process.env.CI,
        },
    ],
})