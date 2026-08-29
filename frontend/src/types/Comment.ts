export type CommentAuthor = {
  id: number;
  username: string;
  displayName: string;
  iconUrl: string | null;
};

export type Comment = {
  id: number;
  content: string;
  author: CommentAuthor;
  parentId: number | null;
  createdAt: string;
};
