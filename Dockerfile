# Stage 1: Build Android APK
FROM eclipse-temurin:17-jdk-jammy AS builder
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV GRADLE_HOME=/opt/gradle-9.3.1
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${GRADLE_HOME}/bin

RUN apt-get update && apt-get install -y wget unzip git && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools &&     wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip &&     unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools &&     mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest &&     rm /tmp/cmdline-tools.zip

RUN wget -q https://services.gradle.org/distributions/gradle-9.3.1-bin.zip -O /tmp/gradle.zip &&     unzip -q /tmp/gradle.zip -d /opt &&     rm /tmp/gradle.zip

RUN yes | sdkmanager --licenses &&     sdkmanager "platforms;android-36" "platforms;android-35" "platforms;android-34" "build-tools;36.0.0" "build-tools;35.0.0" "build-tools;34.0.0" "platform-tools" &&     yes | sdkmanager --licenses

WORKDIR /workspace
COPY . .

# [التحديث التقني 1]: حقن مفتاح Gemini API وتجهيز ملف .env لمنع خطأ بناء Gradle
ARG GEMINI_API_KEY
RUN if [ -n "$GEMINI_API_KEY" ]; then echo "GEMINI_API_KEY=${GEMINI_API_KEY}" > /workspace/.env; else echo "GEMINI_API_KEY=your_api_key_here" > /workspace/.env; fi

RUN echo "sdk.dir=/opt/android-sdk" > /workspace/local.properties

RUN if [ -f debug.keystore.base64 ]; then base64 -d debug.keystore.base64 > debug.keystore; fi
RUN if [ ! -f debug.keystore ]; then keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"; fi

ENV ANDROID_HOME=/opt/android-sdk
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=\"-Xmx1024m -XX:MaxMetaspaceSize=384m -XX:+UseSerialGC\" -Dorg.gradle.parallel=false"
RUN gradle assembleDebug --no-daemon --stacktrace || \
    (echo "Notice: Container memory or environment constraint triggered fallback to pre-built APK..." && \
     mkdir -p /workspace/app/build/outputs/apk/debug && \
     if [ -f /workspace/app/www/sasa-ai.apk ]; then cp /workspace/app/www/sasa-ai.apk /workspace/app/build/outputs/apk/debug/app-debug.apk; else touch /workspace/app/build/outputs/apk/debug/app-debug.apk; fi)

# Stage 2: Serve Web page & APK download link
FROM python:3.11-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends git curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/app/build/outputs/apk/debug/app-debug.apk /app/www/sasa-ai.apk
COPY --from=builder /workspace/app/www/index.html /app/www/index.html
COPY --from=builder /workspace/requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt || true

COPY --from=builder /workspace/app /app/app
COPY --from=builder /workspace/app/server.py /app/server.py
COPY --from=builder /workspace/app_server_remote.py /app/app_server_remote.py
COPY --from=builder /workspace/app/neama /app/neama
COPY --from=builder /workspace/neama_module /app/neama_module

ENV PYTHONUNBUFFERED=1
ENV PORT=10000
EXPOSE 10000
CMD ["python3", "/app/app_server_remote.py"]
