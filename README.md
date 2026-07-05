# Minimal Agent

Spring Boot + LangChain4j + LangGraph4j 的内容生成示例项目。

## 特点

- 真实大模型调用（OpenAI-compatible，默认指向 DashScope）
- LangGraph4j 图执行
- 条件循环：打分不过就回写重试
- 节点状态持久化：PostgreSQL 下使用 checkpoint saver
- 用户只看到最终成品，草稿、评分、trace 只保存在服务端

## 生成流程

`draft -> judge -> revise(循环) -> finalize`

最终下载的文件只包含最终文本。

## 运行

1. 安装 JDK 21
2. 配置 `OPENAI_API_KEY`
3. 需要 PostgreSQL 时，配置 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
4. 本地调试可直接用 H2：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 访问

- `http://localhost:8080/login`
- `http://localhost:8080/register`
- `http://localhost:8080/app`

## 说明

- H2 模式下，图 checkpoint 会回退到内存保存
- PostgreSQL 模式下，checkpoint 会持久化到数据库
