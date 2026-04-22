import { BASE_URL, getHeaders } from './config';

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export async function* streamChat(messages: ChatMessage[]): AsyncGenerator<string, void, unknown> {
  const response = await fetch(`${BASE_URL}/chatbot/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ messages }),
  });

  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`Chatbot error (${response.status}): ${text}`);
  }

  const reader = response.body?.getReader();
  if (!reader) {
    throw new Error('Streaming not supported');
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
      if (!line.trim()) continue;
      try {
        const chunk = JSON.parse(line);
        if (chunk.error) {
          throw new Error(chunk.error);
        }
        if (chunk.message?.content) {
          yield chunk.message.content;
        }
        if (chunk.done) {
          return;
        }
      } catch {
        // ignore malformed lines
      }
    }
  }
}

export async function chatNonStream(messages: ChatMessage[]): Promise<string> {
  const response = await fetch(`${BASE_URL}/chatbot/chat-sync`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ messages }),
  });

  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`Chatbot error (${response.status}): ${text}`);
  }

  const data = await response.json();
  if (!data.success) {
    throw new Error(data.message || 'Request failed');
  }
  return data.data || '';
}
