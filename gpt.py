import socket
import json
import logging
from openai import OpenAI

# 配置日志
logging.basicConfig(level=logging.INFO)

# 初始化OpenAI客户端
client = OpenAI(
    api_key="API-KEY",  # 替换为您实际的API密钥
    base_url="https://api.moonshot.cn/v1",
)

# 创建服务器套接字
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 8888))  # 监听端口8888
server_socket.listen(5)

# 处理客户端请求的函数
def handle_client(client_socket):
    try:
        # 接收客户端请求数据
        request_data = client_socket.recv(1024).decode("utf-8")
        logging.info(f"Received data: {request_data}")

        try:
            data = json.loads(request_data)
            question = data.get("question")
            if not question:
                client_socket.send(json.dumps({"error": "Invalid request: No question provided"}).encode("utf-8"))
                logging.warning("Invalid request: No question provided")
                return

            # 调用GPT模型并获取回答
            response = call_gpt(question)
            logging.info(f"Sending response: {response}")
            client_socket.send(response.encode("utf-8"))

        except json.decoder.JSONDecodeError:
            client_socket.send(json.dumps({"error": "Invalid JSON format"}).encode("utf-8"))
            logging.warning("Invalid JSON format")

    except Exception as e:
        logging.error(f"Error handling client: {e}")
        client_socket.send(json.dumps({"error": "Internal server error"}).encode("utf-8"))
        logging.error("Internal server error")
    finally:
        client_socket.close()

# 调用GPT模型的函数
def call_gpt(question):
    try:
        # 调用Moonshot API并返回回答
        response = client.chat.completions.create(
            model="moonshot-v1-8k",  # 使用moonshot-v1-8k模型
            messages=[
                {"role": "system", "content": "你是 Kimi，由 Moonshot AI 提供的人工智能助手..."},
                {"role": "user", "content": question}
            ],
            temperature=0.3
        )
        return response.choices[0].message.content

    except Exception as e:
        logging.error(f"Error calling GPT model: {e}")
        return "Sorry, an error has occurred while calling GPT model."

# 主函数，启动服务
if __name__ == "__main__":
    logging.info("Server listening on port 8888")
    while True:
        client_socket, client_address = server_socket.accept()
        logging.info(f"Accepted connection from {client_address}")
        handle_client(client_socket)
