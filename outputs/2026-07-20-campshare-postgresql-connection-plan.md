# CampShare PostgreSQL Connection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PostgreSQL 16へ安全に接続し、テーブルを作成せずに接続テストを成功させる。

**Architecture:** Spring Data JPAとPostgreSQL Driverを追加するが、Entity・Repository・MyBatis・Spring Securityはまだ追加しない。DB接続情報はGit管理外の`application-local.properties`だけに置き、`DatabaseConnectionTest`が実際の`DataSource`から取得した接続を検証する。

**Tech Stack:** Java 17、Spring Boot 3.5.16、Maven Wrapper、Spring Data JPA、PostgreSQL Driver、PostgreSQL 16、JUnit 5、AssertJ。

## Global Constraints

- Javaは17、Spring Bootは3.5.16を使用する。
- Windowsではプロジェクト内の`mvnw.cmd`だけを使い、MavenをPCへ個別インストールしない。
- PostgreSQLの接続情報は`application-local.properties`だけに保存し、Git管理しない。
- この計画ではテーブル、JPA Entity、Repository、MyBatis、Spring Securityを作成しない。
- HibernateのDDL自動実行は`none`とし、DBスキーマを変更しない。
- `.git`配下の書き込みが拒否されているため、コミットは実行しない。
- 各タスクの最後でユーザーが手元の動作を確認し、成功報告があるまで次へ進まない。

---

## File Structure

- Modify: `campshare/pom.xml` — JPAとPostgreSQL Driverを追加する。
- Modify: `campshare/.gitignore` — ローカル接続設定とMaven Wrapperのプロジェクト内キャッシュを除外する。
- Modify: `campshare/src/main/resources/application.properties` — 秘密情報を含まない共通JPA設定。
- Create: `campshare/application-local.properties` — ローカルのDB接続情報。Git管理外。
- Create: `campshare/src/test/java/com/example/campshare/config/DatabaseConnectionTest.java` — 実DBへの接続テスト。

---

### Task 2: PostgreSQLへの接続だけを検証する

**Files:**

- Modify: `campshare/pom.xml`
- Modify: `campshare/.gitignore`
- Modify: `campshare/src/main/resources/application.properties`
- Create: `campshare/application-local.properties`
- Create: `campshare/src/test/java/com/example/campshare/config/DatabaseConnectionTest.java`

**Interfaces:**

- Consumes: PostgreSQL 16がローカルで起動していること。
- Produces: Spring Bootテストから`DataSource`を取得し、`PostgreSQL`への有効なJDBC接続を1本確立できること。

- [x] **Step 1: JPA・PostgreSQLの依存関係と安全な共通設定を追加する**

`campshare/pom.xml`の`<dependencies>`内へ、次の2つを追加する。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

`campshare/.gitignore`の末尾に、次の2行を追加する。

```gitignore
application-local.properties
.maven-cache/
```

`campshare/src/main/resources/application.properties`を、次の内容にする。

```properties
spring.application.name=campshare
spring.config.import=optional:file:./application-local.properties
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
```

- [x] **Step 2: 接続失敗を示すテストを先に作成する**

`campshare/src/test/java/com/example/campshare/config/DatabaseConnectionTest.java`を次の内容で作成する。この時点では`application-local.properties`を作成しない。

```java
package com.example.campshare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToPostgreSql() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
    }
}
```

- [x] **Step 3: DB未設定による失敗を確認する**

Run:

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=DatabaseConnectionTest
```

Expected: `Failed to configure a DataSource`を含む`BUILD FAILURE`。DBの接続情報をまだ渡していないため、この失敗は期待どおり。

- [x] **Step 4: ローカルDBと接続情報を作成する**

pgAdminのQuery Toolで、PostgreSQLの管理者ユーザーとして次を実行する。

```sql
CREATE ROLE campshare_app LOGIN PASSWORD 'CampShareLocalOnly_2026!';
CREATE DATABASE campshare OWNER campshare_app;
```

`campshare/application-local.properties`を次の内容で作成する。このファイルは`.gitignore`によりGit管理されない。

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/campshare
spring.datasource.username=campshare_app
spring.datasource.password=CampShareLocalOnly_2026!
```

このパスワードはローカル開発専用であり、提出・公開・共有用の環境では使わない。

- [x] **Step 5: 実DBへの接続テストが成功することを確認する**

Run:

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=DatabaseConnectionTest
```

Expected: `BUILD SUCCESS`。`connectsToPostgreSql`が成功し、PostgreSQL 16への接続だけを検証できる。テーブルは作成されない。

- [x] **Step 6: 手元のブラウザで既存トップページが維持されていることを確認する**

Run:

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd spring-boot:run
```

Expected: コンソールに`Started CampshareApplication`が表示される。ブラウザで`http://localhost:8080/`を開くと、`CampShare`と`キャンプ用品を、もっと手軽に。`が表示される。確認後は起動しているターミナルで`Ctrl + C`を押して停止する。

- [x] **Step 7: ユーザー確認ゲート**

次へ進む前に、ユーザーが以下を確認する。

1. Step 3で、接続情報なしの失敗を確認した。
2. Step 5で、PostgreSQL接続テストの成功を確認した。
3. Step 6で、既存のトップページが表示され、`Ctrl + C`で停止できた。

成功した場合のみ、ユーザーは「Task 2成功」と報告する。報告があるまで、テーブル作成、JPA Entity、Repository、MyBatis、Spring Securityの実装を開始しない。
