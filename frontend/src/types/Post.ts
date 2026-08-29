export type PostAuthor = {
  id: number;
  username: string;
  displayName: string;
  iconUrl: string | null;
};

export type Post = {
  id: number;
  author: PostAuthor;
  title: string;
  description: string | null;
  imageUrl: string;
  whatCreated: string;
  tips: string | null;
  bestPrompt: string | null;
  createdAt: string;
  updatedAt: string;
};