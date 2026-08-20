// Thin wrapper around the STOMP-over-SockJS chat channel described in
// API_CONTRACT.md. Feature code (features/chat) should only ever go through
// this module, never touch @stomp/stompjs directly, so the transport can
// change without touching UI code.

import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Message } from './types';
import { getSession } from './session';

const BASE_URL = import.meta.env.VITE_API_BASE_URL as string;
// API_CONTRACT.md's /ws endpoint hangs off the API host, not under /api.
const WS_URL = BASE_URL.replace(/\/api\/?$/, '') + '/ws';

export interface MatchChatConnection {
  send: (content: string) => void;
  disconnect: () => void;
}

export function connectMatchChat(
  matchId: string,
  onMessage: (message: Message) => void,
): MatchChatConnection {
  const session = getSession();

  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL) as WebSocket,
    connectHeaders: session ? { Authorization: `Bearer ${session.token}` } : {},
    reconnectDelay: 3000,
  });

  client.onConnect = () => {
    client.subscribe(`/topic/matches/${matchId}`, (frame: IMessage) => {
      onMessage(JSON.parse(frame.body) as Message);
    });
  };

  client.activate();

  return {
    send: (content: string) => {
      client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ matchId, content }),
      });
    },
    disconnect: () => {
      void client.deactivate();
    },
  };
}
