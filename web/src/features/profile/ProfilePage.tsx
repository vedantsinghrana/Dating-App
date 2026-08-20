import { type ChangeEvent, useEffect, useRef, useState } from 'react';
import { ApiError } from '../../api/client';
import type { Profile, Prompt } from '../../api/types';
import { PROMPT_QUESTIONS } from './promptQuestions';
import { getMyProfile, updateLocation, updateMyProfile, uploadPhoto } from './profileApi';

const PROMPT_SLOTS = 3;

function emptyPrompt(): Prompt {
  return { id: crypto.randomUUID(), question: PROMPT_QUESTIONS[0], answer: '' };
}

function withPromptSlots(prompts: Prompt[]): Prompt[] {
  const slots = prompts.slice(0, PROMPT_SLOTS);
  while (slots.length < PROMPT_SLOTS) slots.push(emptyPrompt());
  return slots;
}

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveState, setSaveState] = useState<'idle' | 'saving' | 'saved'>('idle');
  const [locationStatus, setLocationStatus] = useState<'idle' | 'requesting' | 'error'>('idle');
  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    getMyProfile()
      .then((p) => setProfile({ ...p, prompts: withPromptSlots(p.prompts) }))
      .catch((err) => setLoadError(err instanceof ApiError ? err.message : 'Failed to load profile.'));
  }, []);

  function update(patch: Partial<Profile>) {
    setProfile((prev) => (prev ? { ...prev, ...patch } : prev));
  }

  function updatePrompt(index: number, patch: Partial<Prompt>) {
    setProfile((prev) => {
      if (!prev) return prev;
      const prompts = prev.prompts.map((p, i) => (i === index ? { ...p, ...patch } : p));
      return { ...prev, prompts };
    });
  }

  async function handlePhotoSelected(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setUploadingPhoto(true);
    setSaveError(null);
    try {
      const { url } = await uploadPhoto(file);
      update({ photos: [...(profile?.photos ?? []), url] });
    } catch (err) {
      setSaveError(err instanceof ApiError ? err.message : 'Photo upload failed.');
    } finally {
      setUploadingPhoto(false);
    }
  }

  function removePhoto(url: string) {
    update({ photos: (profile?.photos ?? []).filter((p) => p !== url) });
  }

  async function requestLocation() {
    if (!navigator.geolocation) {
      setLocationStatus('error');
      return;
    }
    setLocationStatus('requesting');
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const location = { lat: position.coords.latitude, lng: position.coords.longitude };
        try {
          await updateLocation(location);
          update({ location });
          setLocationStatus('idle');
        } catch {
          setLocationStatus('error');
        }
      },
      () => setLocationStatus('error'),
    );
  }

  async function handleSave() {
    if (!profile) return;
    setSaveState('saving');
    setSaveError(null);
    try {
      const { userId, ...update } = profile;
      void userId;
      const saved = await updateMyProfile(update);
      setProfile({ ...saved, prompts: withPromptSlots(saved.prompts) });
      setSaveState('saved');
      setTimeout(() => setSaveState('idle'), 2000);
    } catch (err) {
      setSaveError(err instanceof ApiError ? err.message : 'Failed to save profile.');
      setSaveState('idle');
    }
  }

  if (loadError) {
    return (
      <section className="page">
        <h1>Your profile</h1>
        <p className="form__error" role="alert">{loadError}</p>
      </section>
    );
  }

  if (!profile) {
    return (
      <section className="page">
        <h1>Your profile</h1>
        <p className="page__todo">Loading…</p>
      </section>
    );
  }

  const promptsComplete = profile.prompts.every((p) => p.question && p.answer.trim().length > 0);

  return (
    <section className="page profile-page">
      <h1>Your profile</h1>

      <div className="profile-section">
        <h2>Photos</h2>
        <div className="photo-grid">
          {profile.photos.map((url) => (
            <div key={url} className="photo-grid__item">
              <img src={url} alt="" />
              <button
                type="button"
                className="photo-grid__remove"
                aria-label="Remove photo"
                onClick={() => removePhoto(url)}
              >
                ×
              </button>
            </div>
          ))}
          <button
            type="button"
            className="photo-grid__add"
            onClick={() => fileInputRef.current?.click()}
            disabled={uploadingPhoto}
          >
            {uploadingPhoto ? '…' : '+ Add'}
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            hidden
            onChange={handlePhotoSelected}
          />
        </div>
      </div>

      <div className="profile-section form">
        <h2>About you</h2>
        <label className="form__field">
          <span>Name</span>
          <input value={profile.name} onChange={(e) => update({ name: e.target.value })} />
        </label>
        <label className="form__field">
          <span>Birthdate</span>
          <input
            type="date"
            value={profile.birthdate}
            onChange={(e) => update({ birthdate: e.target.value })}
          />
        </label>
        <label className="form__field">
          <span>Bio</span>
          <textarea
            rows={3}
            value={profile.bio}
            onChange={(e) => update({ bio: e.target.value })}
          />
        </label>
      </div>

      <div className="profile-section">
        <h2>Your 3 prompts</h2>
        <p className="form__hint">
          Pick a question and write your answer. Other people reply to these instead of sending a
          cold-open message.
        </p>
        <div className="prompt-editor">
          {profile.prompts.map((prompt, i) => (
            <div className="prompt-editor__slot" key={prompt.id}>
              <select
                aria-label={`Prompt ${i + 1} question`}
                value={prompt.question}
                onChange={(e) => updatePrompt(i, { question: e.target.value })}
              >
                {PROMPT_QUESTIONS.map((q) => (
                  <option key={q} value={q}>
                    {q}
                  </option>
                ))}
              </select>
              <textarea
                aria-label={`Prompt ${i + 1} answer`}
                rows={2}
                placeholder="Your answer"
                value={prompt.answer}
                onChange={(e) => updatePrompt(i, { answer: e.target.value })}
              />
            </div>
          ))}
        </div>
        {!promptsComplete && <p className="form__hint">Fill in all 3 prompts before saving.</p>}
      </div>

      <div className="profile-section">
        <h2>Location</h2>
        <p className="form__hint">
          {profile.location.lat !== 0 || profile.location.lng !== 0
            ? `Currently set to ${profile.location.lat.toFixed(2)}, ${profile.location.lng.toFixed(2)}`
            : 'No location set yet.'}
        </p>
        <button
          type="button"
          className="button button--secondary"
          onClick={requestLocation}
          disabled={locationStatus === 'requesting'}
        >
          {locationStatus === 'requesting' ? 'Requesting…' : 'Use my current location'}
        </button>
        {locationStatus === 'error' && (
          <p className="form__error" role="alert">
            Couldn't get your location. Check your browser's location permission.
          </p>
        )}

        <label className="form__field" style={{ marginTop: 'var(--space-4)' }}>
          <span>Search radius: {profile.searchRadiusKm} km</span>
          <input
            type="range"
            min={1}
            max={100}
            value={profile.searchRadiusKm}
            onChange={(e) => update({ searchRadiusKm: Number(e.target.value) })}
          />
        </label>
      </div>

      {saveError && <p className="form__error" role="alert">{saveError}</p>}
      <button
        type="button"
        className="button button--primary button--block"
        onClick={handleSave}
        disabled={saveState === 'saving' || !promptsComplete}
      >
        {saveState === 'saving' ? 'Saving…' : saveState === 'saved' ? 'Saved ✓' : 'Save profile'}
      </button>
    </section>
  );
}
