/**
 * AI 服务接口
 * 处理与 AI API 的通信
 */

export interface Message {
    role: 'user' | 'assistant' | 'system';
    content: string;
    timestamp?: number;
}

export interface AIServiceOptions {
    apiBaseUrl?: string;
    model?: string;
    useAliyunModel?: boolean;
}

export class AIService {
    private apiKey: string | null = null;
    private apiBaseUrl: string;
    private model: string;
    private useAliyunModel: boolean;
    private aliyunApiUrl: string = 'https://dashscope.aliyuncs.com/compatible-mode/v1'; // 阿里云API地址

    constructor(options: AIServiceOptions = {}) {
        // 默认使用阿里云模型
        this.useAliyunModel = options.useAliyunModel !== undefined ? options.useAliyunModel : true;

        if (this.useAliyunModel) {
            this.apiBaseUrl = this.aliyunApiUrl;
            this.model = options.model || 'qwen-max-latest';
        } else {
            this.apiBaseUrl = options.apiBaseUrl || 'https://api.openai.com/v1';
            this.model = options.model || 'gpt-4';
        }
    }

    setApiKey(apiKey: string | null) {
        this.apiKey = apiKey;
    }

    toggleModelSource(useAliyun: boolean) {
        this.useAliyunModel = useAliyun;

        if (useAliyun) {
            this.apiBaseUrl = this.aliyunApiUrl;
        } else {
            this.apiBaseUrl = 'https://api.openai.com/v1';
        }
    }

    setModel(model: string) {
        this.model = model;
    }

    // 流式输出方法
    async sendMessageStream(messages: Message[], onChunk: (chunk: string) => void): Promise<void> {
        // 检查API密钥
        if (!this.apiKey) {
            throw new Error('未设置 API Key，请先设置 API Key');
        }

        try {
            const headers: Record<string, string> = {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.apiKey}`,
            };

            console.log('发送流式请求到:', this.apiBaseUrl);
            console.log('使用模型:', this.model);

            const response = await fetch(`${this.apiBaseUrl}/chat/completions`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    model: this.model,
                    messages: messages.map(msg => ({
                        role: msg.role,
                        content: msg.content
                    })),
                    stream: true
                })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(`AI API 请求失败: ${response.status} ${errorData.error?.message || response.statusText}`);
            }

            const reader = response.body?.getReader();
            if (!reader) {
                throw new Error('无法读取响应流');
            }

            const decoder = new TextDecoder();
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();

                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        const data = line.slice(6);

                        if (data === '[DONE]') {
                            return;
                        }

                        try {
                            const parsed = JSON.parse(data);
                            const content = parsed.choices?.[0]?.delta?.content;

                            if (content) {
                                onChunk(content);
                            }
                        } catch (e) {
                            // 忽略解析错误
                        }
                    }
                }
            }
        } catch (error) {
            console.error('AI 流式请求错误:', error);
            throw error;
        }
    }

    // 获取可用模型的方法
    async getAvailableModels(): Promise<string[]> {
        try {
            // 如果是阿里云模型，返回预定义的模型列表
            if (this.useAliyunModel) {
                return [
                    'qwen-max-latest',
                    'qwen-plus-latest',
                    'qwen-turbo-latest',
                    'qwen-long-latest',
                    'qwen-omni-turbo-latest',
                    'qwq-plus-latest'
                ];
            } else {
                // OpenAI 模型列表
                return [
                    'gpt-4.5',
                    'gpt-4.1',
                    'o4-mini',
                    'o3',
                    'o3-pro'
                ];
            }
        } catch (error) {
            console.error('获取模型列表错误:', error);
            // 根据当前使用的服务返回默认模型
            if (this.useAliyunModel) {
                return ['qwen-max-latest', 'qwen-plus-latest', 'qwen-turbo-latest', 'qwen-long-latest', 'qwen-omni-turbo-latest', 'qwq-plus-latest'];
            }
            return ['gpt-4.5',
                'gpt-4.1',
                'o4-mini',
                'o3',
                'o3-pro'];
        }
    }
}

// 导出服务单例，默认使用阿里云模型
export const aiService = new AIService({
    useAliyunModel: true,
    model: 'qwen-max-latest'
});