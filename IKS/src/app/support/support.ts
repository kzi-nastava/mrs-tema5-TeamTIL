import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ViewChild,
  ElementRef,
  AfterViewChecked,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ChatService } from '../services/chat.service';
import { AuthService } from '../services/auth.service';
import { AdminNotification, ChatDTO, MessageDTO } from '../models/chat-dto.models';

@Component({
  selector: 'app-support',
  imports: [CommonModule, FormsModule],
  templateUrl: './support.html',
  styleUrl: './support.css',
})
export class Support implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isAdmin = false;
  userEmail = '';
  newMessage = '';
  isLoading = true;
  searchQuery = '';

  // Admin: list of all chats
  allChats: ChatDTO[] = [];
  selectedChat: ChatDTO | null = null;
  unreadCounts: Record<number, number> = {};

  // User/Driver: their single chat
  myChat: ChatDTO | null = null;
  chatExists = false; // false = no chat created yet
  
  private msgSub?: Subscription;
  private adminNotifSub?: Subscription;
  private shouldScrollToBottom = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }
  
  ngOnInit(): void {
    this.userEmail = this.authService.getEmail() || '';
    this.isAdmin = this.authService.getUserType() === 'ADMINISTRATOR';

    if (this.isAdmin) {
      this.loadAllChats();
      this.connectAdminGlobal();
    } else {
      this.loadExistingChat();
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.msgSub?.unsubscribe();
    this.adminNotifSub?.unsubscribe();
    this.chatService.disconnect();
  }

  // load

  loadAllChats(): void {
    this.isLoading = true;
    this.chatService.getAllChats().subscribe({
      next: (chats) => {
        this.allChats = chats;
        this.isLoading = false;
        if (chats.length > 0) this.selectChat(chats[0]);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading chats:', err);
        this.isLoading = false;
      },
    });
  }

  loadExistingChat(): void {
    this.isLoading = true;
    this.chatService.getExistingChat(this.userEmail).subscribe({
      next: (chat) => {
        this.isLoading = false;
        if (chat) {
          this.myChat = chat;
          this.chatExists = true;
          this.subscribeToChat(chat.id);
          this.shouldScrollToBottom = true;
        } else {
          // null body — no chat started yet, show empty state + input
          this.chatExists = false;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading chat:', err);
        this.isLoading = false;
        this.chatExists = false;
        this.cdr.detectChanges();
      },
    });
  }

  connectAdminGlobal(): void {
    this.chatService.connectAdminGlobal(this.userEmail);

    this.adminNotifSub = this.chatService.adminNotif$.subscribe(
      (notif: AdminNotification) => {
        if (notif.type !== 'NEW_MESSAGE') return;

        const existingIdx = this.allChats.findIndex((c) => c.id === notif.chatId);

        if (existingIdx !== -1) {
          // Update preview
          this.allChats[existingIdx].lastMessageContent = notif.content;
          this.allChats[existingIdx].lastMessageTime = notif.timestamp;
          this.allChats[existingIdx].lastMessageUserType = 'REGISTERED_USER';

          // Move to top
          const updated = this.allChats.splice(existingIdx, 1)[0];
          this.allChats.unshift(updated);

          // Unread badge only if not currently viewing this chat
          if (this.selectedChat?.id !== notif.chatId) {
            this.unreadCounts[notif.chatId] = (this.unreadCounts[notif.chatId] || 0) + 1;
          }
        } else {
          // Brand new chat — reload the list to get user info
          this.chatService.getChatById(notif.chatId).subscribe({
            next: (newChat) => {
              this.allChats.unshift(newChat);
              this.unreadCounts[notif.chatId] = 1;
              this.cdr.detectChanges();
            },
          });
        }
        this.cdr.detectChanges();
      }
    );
  }

  // admin - select chat

  selectChat(chat: ChatDTO): void {
    // Clear unread badge for this chat since we're viewing it now
    delete this.unreadCounts[chat.id];

    // Fetch fresh chat data (including messages) before subscribing to websocket
    this.chatService.getChatById(chat.id).subscribe({
      next: (fresh) => {
        this.selectedChat = fresh;
        const idx = this.allChats.findIndex((c) => c.id === fresh.id);
        if (idx !== -1) this.allChats[idx] = fresh;
        this.subscribeToChat(chat.id);
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      },
    });
  }

  // websocket subscribe

  subscribeToChat(chatId: number): void {
    this.msgSub?.unsubscribe();
    this.chatService.connectToChat(chatId, this.userEmail);

    this.msgSub = this.chatService.message$.subscribe((msg: MessageDTO) => {
      const target = this.isAdmin ? this.selectedChat : this.myChat;
      if (!target || msg.chatId !== target.id) return;

      // Avoid duplicate if we sent it ourselves via HTTP fallback
      const exists = target.messages.some((m) => m.id === msg.id);
      if (!exists) {
        target.messages.push(msg);
        // Update last message in sidebar list
        if (this.isAdmin) {
          const idx = this.allChats.findIndex((c) => c.id === msg.chatId);
          if (idx !== -1) {
            this.allChats[idx].lastMessageContent = msg.content;
            this.allChats[idx].lastMessageTime = msg.timestamp;
          }
        }
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      }
    });
  }

  // send

  sendMessage(): void {
    const trimmed = this.newMessage.trim();
    if (!trimmed) return;

    // ── User/Driver: first message ever
    if (!this.isAdmin && !this.chatExists) {
      this.chatService.startChatWithMessage(this.userEmail, trimmed).subscribe({
        next: (chat) => {
          this.myChat = chat;
          this.chatExists = true;
          this.subscribeToChat(chat.id);
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Failed to start chat:', err),
      });
      this.newMessage = '';
      return;
    }

    // ── Subsequent messages (chat already exists)
    const chatId = this.isAdmin ? this.selectedChat?.id : this.myChat?.id;
    if (!chatId) return;

    this.chatService.sendMessage(chatId, {
      senderEmail: this.userEmail,
      content: trimmed,
    });

    this.newMessage = '';
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  get filteredChats(): ChatDTO[] {
    if (!this.searchQuery.trim()) return this.allChats;
    const q = this.searchQuery.toLowerCase();
    return this.allChats.filter(
      (c) =>
        c.userFirstName.toLowerCase().includes(q) ||
        c.userLastName.toLowerCase().includes(q) ||
        c.userEmail.toLowerCase().includes(q)
    );
  }

  get activeMessages(): MessageDTO[] {
    return (this.isAdmin ? this.selectedChat?.messages : this.myChat?.messages) ?? [];
  }

  isMyMessage(msg: MessageDTO): boolean {
    if (this.isAdmin) return msg.userType === 'ADMINISTRATOR';
    return msg.userType !== 'ADMINISTRATOR';
  }

  getRoleLabel(userType: string): string {
    switch (userType) {
      case 'DRIVER': return 'driver';
      case 'REGISTERED_USER': return 'user';
      case 'ADMINISTRATOR': return 'admin';
      default: return userType?.toLowerCase() ?? '';
    }
  }

  getUnreadCount(chatId: number): number {
    return this.unreadCounts[chatId] || 0;
  }

  getLastMessagePrefix(chat: ChatDTO): string {
    if (!chat.lastMessageUserType) return '';
    return chat.lastMessageUserType === 'ADMINISTRATOR'
      ? 'You'
      : chat.userFirstName;
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (_) {}
  }
}
