# kotlin-cv.js terminal

dev:
    ./gradlew jsBrowserDevelopmentRun --continuous

build:
    ./gradlew jsBrowserProductionWebpack

test-unit:
    ./gradlew jsTest

test-e2e:
    cd e2e && npm install && npx playwright install chromium && npx playwright test --project=chromium

test-e2e-headed:
    cd e2e && npx playwright test --project=chromium --headed --workers=1

test: test-unit test-e2e

test-ui:
    cd e2e && npx playwright test --ui

clean:
    ./gradlew clean
    rm -rf e2e/node_modules e2e/playwright-report e2e/test-results

setup:
    cd e2e && npm install && npx playwright install


