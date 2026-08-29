import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { UserProfile } from "../types/Profile";
import { fetchProfile } from "../api/profileApi";
import { followUser, unfollowUser } from "../api/followApi";
import ProfileEditForm from "../components/profile/ProfileEditForm";
import type { AuthState } from "../hooks/useAuth";

type ProfilePageProps = {
  auth: AuthState;
};

function ProfilePage({ auth }: ProfilePageProps) {
  const { username } = useParams<{ username: string }>();
  const [profile, setProfile] = useState<UserProfile | null | undefined>(undefined);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [followSubmitting, setFollowSubmitting] = useState(false);

  useEffect(() => {
    if (!username) {
      return;
    }

    setProfile(undefined);
    setError(null);
    setEditing(false);

    fetchProfile(username)
      .then(setProfile)
      .catch((err) => {
        setError(err instanceof Error ? err.message : "プロフィールの取得に失敗しました");
        setProfile(null);
      });
  }, [username]);

  const handleToggleFollow = async () => {
    if (!profile) {
      return;
    }

    const currentlyFollowed = profile.followedByCurrentUser;

    setProfile({
      ...profile,
      followedByCurrentUser: !currentlyFollowed,
      followerCount: profile.followerCount + (currentlyFollowed ? -1 : 1),
    });
    setFollowSubmitting(true);
    setError(null);

    try {
      if (currentlyFollowed) {
        await unfollowUser(profile.id);
      } else {
        await followUser(profile.id);
      }
    } catch (err) {
      setProfile({
        ...profile,
        followedByCurrentUser: currentlyFollowed,
        followerCount: profile.followerCount,
      });
      setError(err instanceof Error ? err.message : "フォロー操作に失敗しました");
    } finally {
      setFollowSubmitting(false);
    }
  };

  const handleProfileUpdated = (updated: UserProfile) => {
    setProfile(updated);
    setEditing(false);

    if (auth.user && auth.user.username === updated.username) {
      auth.updateUser({
        displayName: updated.displayName,
        iconUrl: updated.iconUrl,
        bio: updated.bio,
      });
    }
  };

  if (profile === undefined) {
    return <p className="profile-page__status">読み込み中...</p>;
  }

  if (profile === null) {
    return <p className="profile-page__status">ユーザーが見つかりません</p>;
  }

  const isOwnProfile = auth.user?.username === profile.username;

  return (
    <section className="profile-page">
      {error && <p className="profile-page__error">{error}</p>}

      <div className="profile-page__header">
        {profile.iconUrl && (
          <img
            className="profile-page__icon"
            src={profile.iconUrl}
            alt=""
            onError={(e) => {
              e.currentTarget.style.display = "none";
            }}
          />
        )}

        <div>
          <h2 className="profile-page__display-name">{profile.displayName}</h2>
          <p className="profile-page__username">@{profile.username}</p>
        </div>

        {isOwnProfile ? (
          <button type="button" className="profile-page__edit" onClick={() => setEditing((prev) => !prev)}>
            {editing ? "キャンセル" : "編集"}
          </button>
        ) : (
          auth.user && (
            <button
              type="button"
              className={
                profile.followedByCurrentUser ? "post-card__unfollow" : "post-card__follow"
              }
              disabled={followSubmitting}
              onClick={handleToggleFollow}
            >
              {profile.followedByCurrentUser ? "フォロー中" : "フォローする"}
            </button>
          )
        )}
      </div>

      {profile.bio && <p className="profile-page__bio">{profile.bio}</p>}

      <div className="profile-page__stats">
        <span>フォロワー {profile.followerCount}</span>
        <span>フォロー中 {profile.followingCount}</span>
      </div>

      <div className="profile-page__ai-tools">
        <h3>よく使うAI</h3>
        {profile.aiTools.length > 0 ? (
          <ul className="profile-page__ai-tool-list">
            {profile.aiTools.map((tool) => (
              <li key={tool.id}>{tool.name}</li>
            ))}
          </ul>
        ) : (
          <p className="profile-page__ai-tool-empty">未設定</p>
        )}
      </div>

      {isOwnProfile && editing && (
        <ProfileEditForm profile={profile} onUpdated={handleProfileUpdated} />
      )}
    </section>
  );
}

export default ProfilePage;
