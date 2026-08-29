export type User = {
  id: number;
  username: string;
  displayName: string;
  email: string;
  iconUrl: string | null;
  bio: string | null;
  followerCount: number;
  followingCount: number;
  followedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
};
