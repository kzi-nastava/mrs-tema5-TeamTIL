export interface MessageDTO {
  id?: number;
  content: string;
  timestamp: string; // HH:mm
  date: string // YYYY-MM-DD
  userType: 'ADMINISTRATOR' | 'REGISTERED_USER' | 'DRIVER';
  chatId: number;
}

export interface ChatDTO {
  id: number;
  userEmail: string;
  userFirstName: string;
  userLastName: string;
  userType: 'REGISTERED_USER' | 'DRIVER';
  messages: MessageDTO[];
  lastMessageContent?: string;
  lastMessageTime?: string;
  lastMessageUserType?: 'ADMINISTRATOR' | 'REGISTERED_USER' | 'DRIVER';
}

export interface SendMessageRequest {
  senderEmail: string;
  content: string;
}

export interface AdminNotification {
  type: 'NEW_MESSAGE';
  chatId: number;
  content: string;
  timestamp: string;
}