import { api } from '../../api/client';
import type { Message, MessagesResponse, SendMessageRequest } from '../../api/types';

export function getMessages(matchId: string) {
  return api.get<MessagesResponse>(`/matches/${matchId}/messages`);
}

export function sendMessage(matchId: string, body: SendMessageRequest) {
  return api.post<Message>(`/matches/${matchId}/messages`, body);
}
