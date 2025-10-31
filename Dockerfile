# 🧱 Step 1: Use official lightweight Java image
FROM openjdk:17-jdk-slim

# 🏗️ Step 2: Set working directory
WORKDIR /app

# 📂 Step 3: Copy source, lib, and compile output folders
COPY src/ /app/src/
COPY lib/ /app/lib/

# 🔧 Step 4: Create bin folder (kung wala pa)
RUN mkdir -p /app/bin

# 🏗️ Step 5: Compile all Java source files
RUN javac -d bin -cp "lib/mysql-connector-j-9.5.0.jar" src/*.java

# 🚀 Step 6: Run the main class
CMD ["java", "-cp", "bin:lib/mysql-connector-j-9.5.0.jar", "Main"]
