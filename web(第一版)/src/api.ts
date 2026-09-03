import axios from 'axios';

export const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 20000,
});

export async function streamConsultation(payload: any, onChunk: (text: string) => void) {
  const response = await fetch('http://localhost:8080/api/consultation/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.body) return;
  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    const chunk = decoder.decode(value, { stream: true });
    onChunk(chunk.replace(/data:/g, '').replace(/event:message/g, '').trim());
  }
}
