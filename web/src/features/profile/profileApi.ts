import { api } from '../../api/client';
import type { GeoPoint, PhotoUploadResponse, Profile, ProfileUpdate } from '../../api/types';

export function getMyProfile() {
  return api.get<Profile>('/profiles/me');
}

export function updateMyProfile(update: ProfileUpdate) {
  return api.put<Profile>('/profiles/me', update);
}

export function uploadPhoto(file: File) {
  const form = new FormData();
  form.append('file', file);
  return api.postForm<PhotoUploadResponse>('/profiles/me/photos', form);
}

export function updateLocation(location: GeoPoint) {
  return api.put<void>('/profiles/me/location', location);
}
