import { Client } from '@stomp/stompjs';
import { NotificationItem } from '../types';

/**
 * sockjs-client is a CJS-only package. We import it via a dynamic import()
 * so Vite's dep pre-bundler wraps it into ESM without hitting the global
 * `require` that breaks native ESM in the browser.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let _SockJS: any = null;
async function getSockJS() {
  if (!_SockJS) {
    const mod = await import('sockjs-client');
    _SockJS = mod.default ?? mod;
  }
  return _SockJS;
}

class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;

  async connect(userId: number, onNotificationReceived: (notification: NotificationItem) => void) {
    if (this.isConnected && this.client) {
      return;
    }

    try {
      const SockJS = await getSockJS();

      this.client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          this.isConnected = true;
          // Subscribe to user personal notification channel
          this.client?.subscribe(`/topic/notifications/${userId}`, (message) => {
            try {
              const notification: NotificationItem = JSON.parse(message.body);
              onNotificationReceived(notification);
            } catch (err) {
              console.error('Failed to parse websocket notification', err);
            }
          });

          // Subscribe to general notifications broadcast
          this.client?.subscribe('/topic/notifications', (message) => {
            try {
              const notification: NotificationItem = JSON.parse(message.body);
              onNotificationReceived(notification);
            } catch (err) {
              console.error('Failed to parse broadcast notification', err);
            }
          });
        },
        onDisconnect: () => {
          this.isConnected = false;
        },
        onStompError: (frame) => {
          console.warn('STOMP protocol error:', frame.headers['message']);
        },
      });

      this.client.activate();
    } catch (err) {
      console.warn('WebSocket connection not supported or server unavailable, falling back to REST polling:', err);
    }
  }

  disconnect() {
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (e) {
        console.warn('Error during stomp deactivation', e);
      }
      this.isConnected = false;
      this.client = null;
    }
  }
}

export const websocketService = new WebSocketService();
