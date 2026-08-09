# Stage 1: Build Android APK
FROM eclipse-temurin:17-jdk-jammy AS builder

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV GRADLE_HOME=/opt/gradle-9.3.1
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${GRADLE_HOME}/bin

RUN apt-get update && apt-get install -y wget unzip git && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

RUN wget -q https://services.gradle.org/distributions/gradle-9.3.1-bin.zip -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip

RUN yes | sdkmanager --licenses && \
    sdkmanager "platforms;android-36" "platforms;android-34" "build-tools;34.0.0" "platform-tools"

WORKDIR /workspace
COPY . .
RUN if [ -f debug.keystore.base64 ]; then base64 -d debug.keystore.base64 > debug.keystore; fi
RUN gradle assembleDebug --no-daemon

# Stage 2: Serve Web page & APK download link
FROM python:3.11-slim

WORKDIR /app
COPY --from=builder /workspace/app/build/outputs/apk/debug/app-debug.apk /app/www/sasa-ai.apk
COPY app/www/index.html /app/www/index.html
COPY app/server.py /app/server.py

EXPOSE 10000

CMD ["python3", "/app/server.py"]
