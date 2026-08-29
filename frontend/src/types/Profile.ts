export type AiTool = {
  id: number;
  name: string;
};

export type UserProfile = {
  id: number;
  username: string;
  displayName: string;
  iconUrl: string | null;
  bio: string | null;
  followerCount: number;
  followingCount: number;
  followedByCurrentUser: boolean;
  aiTools: AiTool[];
};

export type ProfileUpdateValues = {
  displayName: string;
  iconUrl: string;
  bio: string;
};
