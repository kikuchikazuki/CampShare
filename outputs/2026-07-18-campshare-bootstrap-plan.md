# CampShare Bootstrap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PostgreSQLや認証機能をまだ追加せず、CampShareの最小Spring Bootアプリをローカルで起動し、トップページを表示する。

**Architecture:** この計画ではWeb層だけを作る。`HomeController`が`GET /`を受け取り、Thymeleafテンプレート`home.html`を返す。JPA、MyBatis、Spring Security、PostgreSQLへの接続は次の計画で追加し、この段階では外部データベースや認証設定を変更しない。

**Tech Stack:** Java 17、Spring Boot 3.5.16、Maven Wrapper、Spring Web、Thymeleaf、Spring Boot DevTools、Lombok、JUnit 5、MockMvc。

## Global Constraints

- Javaは17を使用する。
- MavenをPCへ個別インストールしない。Windowsではプロジェクト内の`mvnw.cmd`だけを使う。
- プロジェクトのベースパッケージは`com.example.campshare`とする。
- この計画では`Spring Data JPA`、`MyBatis Framework`、`Spring Security`、PostgreSQL Driver、H2 Databaseを追加しない。
- DBパスワード、APIキーなどの秘密情報をファイルへ保存しない。
- `.git`配下の書き込みが拒否されているため、コミットは実行しない。各ステップの前後でユーザーが手動確認できる状態を保つ。
- 各タスクの最後でユーザーが確認し、成功報告があるまで次のタスクを実行しない。

---

## File Structure

- Create: `pom.xml` — Maven設定と最小依存関係。
- Create: `.mvn/wrapper/maven-wrapper.properties`、`.mvn/wrapper/maven-wrapper.jar`、`mvnw`、`mvnw.cmd` — Maven Wrapper。
- Create: `.gitignore` — ビルド成果物・IDE設定・将来の環境変数ファイルをGit対象外にする。
- Create: `src/main/java/com/example/campshare/CampShareApplication.java` — Spring Bootのエントリーポイント。
- Create: `src/main/java/com/example/campshare/web/HomeController.java` — トップページのHTTPエンドポイント。
- Create: `src/main/resources/templates/home.html` — Thymeleafトップページ。
- Create: `src/test/java/com/example/campshare/web/HomeControllerTest.java` — トップページのWeb層テスト。

---

### Task 1: 最小トップページを起動する

**Files:**

- Create: `pom.xml`
- Create: `.gitignore`
- Create: `src/main/java/com/example/campshare/CampShareApplication.java`
- Create: `src/main/java/com/example/campshare/web/HomeController.java`
- Create: `src/main/resources/templates/home.html`
- Create: `src/test/java/com/example/campshare/web/HomeControllerTest.java`

**Interfaces:**

- Consumes: なし。
- Produces: `GET /` がHTTP 200とビュー名`home`を返す。後続の画面・認証機能はこのURLを起点として追加する。

- [x] **Step 1: Maven設定、エントリーポイント、Git除外設定を作成する**

Spring Initializrから、Maven Wrapperを含む最小プロジェクトを生成する。実行場所はプロジェクトルートとする。

```powershell
Invoke-WebRequest `
  -Uri "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.5.16&baseDir=campshare&groupId=com.example&artifactId=campshare&name=campshare&description=Camp%20gear%20rental%20application&packageName=com.example.campshare&packaging=jar&javaVersion=17&dependencies=web,thymeleaf,devtools,lombok" `
  -OutFile "campshare.zip"
Expand-Archive -Path "campshare.zip" -DestinationPath "." -Force
Remove-Item "campshare.zip"
Set-Location "campshare"
```

Expected: `mvnw`、`mvnw.cmd`、`.mvn/wrapper/`、`pom.xml`、`src/`が作成される。以後の全コマンドは、この`campshare`ディレクトリで実行する。

生成された`pom.xml`は次の内容であることを確認する。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.16</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>campshare</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>campshare</name>
    <description>Camp gear rental application</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

`.gitignore`を次の内容で作成する。

```gitignore
target/
.mvn/
.idea/
.vscode/
*.iml
.env
application-local.properties
```

`src/main/java/com/example/campshare/CampShareApplication.java`を次の内容で作成する。

```java
package com.example.campshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CampShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampShareApplication.class, args);
    }
}
```

- [x] **Step 2: トップページの失敗するWebテストを作成する**

`src/test/java/com/example/campshare/web/HomeControllerTest.java`を次の内容で作成する。この時点ではControllerを作らない。

```java
package com.example.campshare.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void topPageReturnsHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}
```

- [x] **Step 3: テストが失敗することを確認する**

Run:

```powershell
.\mvnw.cmd test -Dtest=HomeControllerTest
```

Expected: `topPageReturnsHomeView`がHTTP 404のため失敗する。`BUILD FAILURE`は、この段階では期待どおり。

- [x] **Step 4: Controllerとテンプレートを最小実装する**

`src/main/java/com/example/campshare/web/HomeController.java`を次の内容で作成する。

```java
package com.example.campshare.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String topPage() {
        return "home";
    }
}
```

`src/main/resources/templates/home.html`を次の内容で作成する。

```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CampShare</title>
</head>
<body>
<main>
    <h1>CampShare</h1>
    <p>キャンプ用品を、もっと手軽に。</p>
</main>
</body>
</html>
```

- [x] **Step 5: テストが成功することを確認する**

Run:

```powershell
.\mvnw.cmd test -Dtest=HomeControllerTest
```

Expected: `BUILD SUCCESS`。`Tests run: 1, Failures: 0, Errors: 0`が表示される。

- [x] **Step 6: 手元のブラウザでトップページを確認する**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: コンソールに`Started CampShareApplication`が表示される。ブラウザで`http://localhost:8080/`を開くと、`CampShare`と`キャンプ用品を、もっと手軽に。`が表示される。確認後は、起動しているターミナルで`Ctrl + C`を押して停止する。

- [x] **Step 7: ユーザー確認ゲート**

次へ進む前に、ユーザーが以下を確認する。

1. Step 5のテストが成功した。
2. Step 6のブラウザ表示が確認できた。
3. `Ctrl + C`でアプリを停止できた。

成功した場合のみ、ユーザーは「Task 1成功」と報告する。報告があるまで、PostgreSQL接続、JPA、MyBatis、Spring Security、ドメインテーブルの作成を開始しない。
