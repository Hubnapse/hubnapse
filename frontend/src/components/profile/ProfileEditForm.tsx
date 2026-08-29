import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import type { AiTool, UserProfile } from "../../types/Profile";
import { fetchAiTools } from "../../api/aiToolApi";
import { updateAiTools, updateProfile } from "../../api/profileApi";

type ProfileEditFormProps = {
  profile: UserProfile;
  onUpdated: (updated: UserProfile) => void;
};

function ProfileEditForm({ profile, onUpdated }: ProfileEditFormProps) {
  const [displayName, setDisplayName] = useState(profile.displayName);
  const [iconUrl, setIconUrl] = useState(profile.iconUrl ?? "");
  const [bio, setBio] = useState(profile.bio ?? "");
  const [aiToolOptions, setAiToolOptions] = useState<AiTool[]>([]);
  const [selectedAiToolIds, setSelectedAiToolIds] = useState<number[]>(
    profile.aiTools.map((tool) => tool.id),
  );
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAiTools()
      .then(setAiToolOptions)
      .catch((err) => {
        setError(err instanceof Error ? err.message : "AIツール一覧の取得に失敗しました");
      });
  }, []);

  const handleToggleAiTool = (toolId: number) => {
    setSelectedAiToolIds((prev) =>
      prev.includes(toolId) ? prev.filter((id) => id !== toolId) : [...prev, toolId],
    );
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const updatedUser = await updateProfile({ displayName, iconUrl, bio });
      const updatedAiTools = await updateAiTools(selectedAiToolIds);

      onUpdated({
        ...profile,
        displayName: updatedUser.displayName,
        iconUrl: updatedUser.iconUrl,
        bio: updatedUser.bio,
        aiTools: updatedAiTools,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "プロフィールの更新に失敗しました");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="profile-edit-form" onSubmit={handleSubmit}>
      {error && <p className="profile-edit-form__error">{error}</p>}

      <div className="profile-edit-form__field">
        <label htmlFor="profile-display-name">表示名（最大50文字）</label>
        <input
          id="profile-display-name"
          type="text"
          required
          maxLength={50}
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
        />
      </div>

      <div className="profile-edit-form__field">
        <label htmlFor="profile-icon-url">アイコンURL</label>
        <input
          id="profile-icon-url"
          type="text"
          maxLength={500}
          value={iconUrl}
          onChange={(e) => setIconUrl(e.target.value)}
        />
      </div>

      <div className="profile-edit-form__field">
        <label htmlFor="profile-bio">自己紹介（最大300文字）</label>
        <textarea
          id="profile-bio"
          maxLength={300}
          value={bio}
          onChange={(e) => setBio(e.target.value)}
        />
      </div>

      <div className="profile-edit-form__field">
        <span className="profile-edit-form__label">よく使うAI</span>
        <div className="profile-edit-form__ai-tools">
          {aiToolOptions.map((tool) => (
            <label key={tool.id} className="profile-edit-form__ai-tool-option">
              <input
                type="checkbox"
                checked={selectedAiToolIds.includes(tool.id)}
                onChange={() => handleToggleAiTool(tool.id)}
              />
              {tool.name}
            </label>
          ))}
        </div>
      </div>

      <button type="submit" disabled={submitting}>
        {submitting ? "保存中..." : "保存する"}
      </button>
    </form>
  );
}

export default ProfileEditForm;
