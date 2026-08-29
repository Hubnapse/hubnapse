export type Post = {
  id: number;
  title: string;
  description: string | null;
  imageUrl: string;
  whatCreated: string;
  tips: string | null;
  bestPrompt: string | null;
  createdAt: string;
  updatedAt: string;
};