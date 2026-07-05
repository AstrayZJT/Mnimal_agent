# LangGraph4j Demo

一个 Spring Boot + LangGraph4j + LangChain4j 的学习项目，补了前端、用户系统和生成结果落盘。

## 功能

- 注册 / 登录 / 退出
- 生成内容并保存到数据库
- 自动把最终文本写入磁盘
- 历史记录浏览、复制、下载

## 运行

本地演示建议用 H2 profile：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

默认配置保留为 PostgreSQL，可按你的环境变量直接切换：

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `OPENAI_API_KEY`

## 输出

生成文件会落到 `generated-output/` 下。

