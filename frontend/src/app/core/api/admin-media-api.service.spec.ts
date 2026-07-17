import { AdminMediaApiService, MediaUploadProgress } from './admin-media-api.service';

describe('AdminMediaApiService', () => {
  it('exposes upload progress shape used by the UI', () => {
    const progress: MediaUploadProgress = {
      progress: 100,
      response: {
        url: 'http://localhost/media/a.png',
        publicId: 'local/a.png',
        provider: 'local',
        format: 'png',
        contentType: 'image/png',
        sizeBytes: 10,
        width: 1,
        height: 1,
      },
    };
    expect(progress.progress).toBe(100);
    expect(progress.response?.publicId).toBe('local/a.png');
    expect(AdminMediaApiService).toBeTruthy();
  });
});
