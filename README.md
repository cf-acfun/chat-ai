# 项目目标
基于 Spring AI 的 AI 客服：支持多轮会话、业务数据查询、知识库问答，并具备评估、降级和成本统计能力。

# 当前能力
1. MiniMax 单次同步对话
2. OpenAI 风格的流式响应接口

# 当前问题
1. 没有会话 ID
2. 请求历史被丢弃，无法多轮对话
3. Controller 直接依赖 MiniMaxChatModel
4. 暂无业务数据查询和知识库
5. 暂无自动化测试与效果评估

# 实验记录
## 模型重复输出问题
详见：src/main/java/chat/testai/chatai/todo/todoList.md
