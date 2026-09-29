# Reproducible Android build environment, not an app server.
FROM eclipse-temurin:17-jdk-jammy

ARG ANDROID_CMDLINE_TOOLS=13114758
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p "$ANDROID_HOME/cmdline-tools" \
    && curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS}_latest.zip" -o /tmp/tools.zip \
    && unzip -q /tmp/tools.zip -d /tmp/android-tools \
    && mv /tmp/android-tools/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest" \
    && rm -rf /tmp/tools.zip /tmp/android-tools \
    && yes | sdkmanager --licenses >/dev/null \
    && sdkmanager --install 'platforms;android-35' 'build-tools;35.0.0' 'build-tools;34.0.0' 'platform-tools'

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
CMD ["./gradlew", "testDebugUnitTest", "assembleDebug", "--no-daemon", "--console=plain"]
