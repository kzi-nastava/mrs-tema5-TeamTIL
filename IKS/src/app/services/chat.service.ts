import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { AdminNotification, ChatDTO, MessageDTO, SendMessageRequest } from '../models/chat-dto.models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = environment.apiUrl;
  private readonly wsBase = environment.wsBase;

    private chatSocket: WebSocket | null = null;
    private messageSubject = new Subject<MessageDTO>();
    public message$ = this.messageSubject.asObservable();

    private adminSocket: WebSocket | null = null;
    private adminNotifSubject = new Subject<AdminNotification>();
    public adminNotif$ = this.adminNotifSubject.asObservable();

    constructor(private http: HttpClient, private zone: NgZone) { }
    
    getExistingChat(email: string): Observable<ChatDTO | null> {
        return this.http.get<ChatDTO | null>(
            `${this.apiUrl}/chat/my/exists`,
            { params: { email } }
        );
    }

    getAllChats(): Observable<ChatDTO[]> {
        return this.http.get<ChatDTO[]>(`${this.apiUrl}/chat/all`);
    }

    getChatById(chatId: number): Observable<ChatDTO> {
        return this.http.get<ChatDTO>(`${this.apiUrl}/chat/${chatId}`);
    }

    startChatWithMessage(email: string, content: string): Observable<ChatDTO> {
        return this.http.post<ChatDTO>(
            `${this.apiUrl}/chat/my/start`,
            { senderEmail: email, content },
            { params: { email } }
        );
    }

    connectToChat(chatId: number, email: string): void {
        this.disconnectChat();
        const url = `${this.wsBase}/ws/chat?chatId=${chatId}&email=${encodeURIComponent(email)}`;
        this.chatSocket = new WebSocket(url);

        this.chatSocket.onmessage = (event: MessageEvent) => {
            try {
                const msg: MessageDTO = JSON.parse(event.data);
                this.zone.run(() => this.messageSubject.next(msg));
            } catch (e) {
                console.error('WS chat parse error:', e);
            }
        };
        this.chatSocket.onerror = (err) => console.error('WS chat error:', err);
        this.chatSocket.onclose = () => console.log('WS chat closed for', chatId);
    }

    sendMessage(chatId: number, request: SendMessageRequest): void {
        const payload = JSON.stringify(request);
        if (this.chatSocket && this.chatSocket.readyState === WebSocket.OPEN) {
            this.chatSocket.send(payload);
        } else {
            this.sendMessageHttp(chatId, request).subscribe({
                next: (msg) => this.messageSubject.next(msg),
                error: (err) => console.error('HTTP fallback failed:', err),
            });
        }
    }

    sendMessageHttp(chatId: number, request: SendMessageRequest): Observable<MessageDTO> {
        return this.http.post<MessageDTO>(`${this.apiUrl}/chat/${chatId}/messages`, request);
    }

    disconnectChat(): void {
        if (this.chatSocket) {
            this.chatSocket.close();
            this.chatSocket = null;
        }
    }

    connectAdminGlobal(email: string): void {
        this.disconnectAdmin();
        const url = `${this.wsBase}/ws/chat/admin?email=${encodeURIComponent(email)}`;
        this.adminSocket = new WebSocket(url);

        this.adminSocket.onmessage = (event: MessageEvent) => {
        try {
            const notif: AdminNotification = JSON.parse(event.data);
            this.zone.run(() => this.adminNotifSubject.next(notif));
        } catch (e) {
            console.error('WS admin parse error:', e);
        }
        };
        this.adminSocket.onerror = (err) => console.error('WS admin error:', err);
        this.adminSocket.onclose = () => console.log('WS admin closed');
    }

    disconnectAdmin(): void {
        if (this.adminSocket) {
            this.adminSocket.close();
            this.adminSocket = null;
        }
    }

    disconnect(): void {
        this.disconnectChat();
        this.disconnectAdmin();
    }
}