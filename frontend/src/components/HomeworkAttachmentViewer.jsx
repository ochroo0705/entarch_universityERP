import { useEffect, useMemo, useState } from 'react';
import { downloadFileAuthenticated, fetchAuthenticatedFileBlobUrl } from '../api/endpoints';

const IMAGE_MIME_PREFIX = 'image/';

export default function HomeworkAttachmentViewer({ attachments = [], compact = false }) {
  const normalizedAttachments = useMemo(
    () => attachments.filter(Boolean).map((attachment, index) => ({
      ...attachment,
      stableKey: attachment.id ?? `${attachment.kind || 'attachment'}-${attachment.originalFilename || 'file'}-${index}`,
    })),
    [attachments]
  );

  const imageAttachments = useMemo(
    () => normalizedAttachments.filter((attachment) => attachment.mimeType?.startsWith(IMAGE_MIME_PREFIX) && attachment.previewUrl),
    [normalizedAttachments]
  );

  const [imageBlobUrls, setImageBlobUrls] = useState({});
  const [previewingAttachment, setPreviewingAttachment] = useState(null);
  const [previewBlobUrl, setPreviewBlobUrl] = useState('');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const createdUrls = [];

    const loadImages = async () => {
      const nextUrls = {};
      for (const attachment of imageAttachments) {
        try {
          const { blobUrl } = await fetchAuthenticatedFileBlobUrl(attachment.previewUrl);
          if (cancelled) {
            URL.revokeObjectURL(blobUrl);
            return;
          }
          createdUrls.push(blobUrl);
          nextUrls[attachment.stableKey] = blobUrl;
        } catch {
          // Fall back to download-only behavior for images we cannot inline.
        }
      }
      if (!cancelled) {
        setImageBlobUrls(nextUrls);
      }
    };

    if (imageAttachments.length > 0) {
      loadImages();
    } else {
      setImageBlobUrls({});
    }

    return () => {
      cancelled = true;
      createdUrls.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [imageAttachments]);

  useEffect(() => () => {
    if (previewBlobUrl) {
      URL.revokeObjectURL(previewBlobUrl);
    }
  }, [previewBlobUrl]);

  const openPreview = async (attachment) => {
    if (!attachment.previewUrl) {
      downloadFileAuthenticated(attachment.downloadUrl);
      return;
    }

    setPreviewingAttachment(attachment);
    setPreviewLoading(true);
    setPreviewError('');

    if (previewBlobUrl) {
      URL.revokeObjectURL(previewBlobUrl);
      setPreviewBlobUrl('');
    }

    try {
      const { blobUrl } = await fetchAuthenticatedFileBlobUrl(attachment.previewUrl);
      setPreviewBlobUrl(blobUrl);
    } catch {
      setPreviewError('Preview unavailable. You can still download the file.');
    } finally {
      setPreviewLoading(false);
    }
  };

  const closePreview = () => {
    if (previewBlobUrl) {
      URL.revokeObjectURL(previewBlobUrl);
    }
    setPreviewBlobUrl('');
    setPreviewingAttachment(null);
    setPreviewError('');
    setPreviewLoading(false);
  };

  if (normalizedAttachments.length === 0) {
    return null;
  }

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', gap: compact ? '0.5rem' : '0.75rem' }}>
        <div style={{ fontSize: compact ? '0.78rem' : '0.84rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
          Attachments
        </div>
        <div style={{ display: 'grid', gap: '0.75rem', gridTemplateColumns: compact ? 'repeat(auto-fit, minmax(180px, 1fr))' : 'repeat(auto-fit, minmax(220px, 1fr))' }}>
          {normalizedAttachments.map((attachment) => {
            const isImage = attachment.mimeType?.startsWith(IMAGE_MIME_PREFIX);
            const isPdf = attachment.mimeType === 'application/pdf';
            const imageBlobUrl = imageBlobUrls[attachment.stableKey];

            return (
              <div key={attachment.stableKey} style={{
                border: '1px solid var(--border)',
                borderRadius: '10px',
                background: 'white',
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column',
                minHeight: compact ? '160px' : '210px',
              }}>
                <div style={{
                  background: isImage ? '#F8FAFC' : '#FFF7ED',
                  minHeight: compact ? '90px' : '130px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '0.75rem',
                }}>
                  {isImage && imageBlobUrl ? (
                    <img
                      src={imageBlobUrl}
                      alt={attachment.originalFilename}
                      style={{ maxWidth: '100%', maxHeight: compact ? '90px' : '130px', objectFit: 'contain', borderRadius: '6px', cursor: attachment.previewable ? 'zoom-in' : 'default' }}
                      onClick={() => attachment.previewable && openPreview(attachment)}
                    />
                  ) : isPdf ? (
                    <button className="btn btn-secondary btn-sm" onClick={() => openPreview(attachment)}>
                      Preview PDF
                    </button>
                  ) : (
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center' }}>
                      Preview unavailable
                    </span>
                  )}
                </div>

                <div style={{ padding: '0.75rem', display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
                  <div>
                    <div style={{ fontSize: '0.82rem', fontWeight: 600, wordBreak: 'break-word' }}>{attachment.originalFilename || 'Attachment'}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {attachment.mimeType || 'File'}
                      {attachment.size ? ` • ${formatFileSize(attachment.size)}` : ''}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '0.5rem', marginTop: 'auto', flexWrap: 'wrap' }}>
                    {attachment.previewable && (
                      <button className="btn btn-secondary btn-sm" onClick={() => openPreview(attachment)}>
                        {isImage ? 'View' : 'Preview'}
                      </button>
                    )}
                    <button className="btn btn-primary btn-sm" onClick={() => downloadFileAuthenticated(attachment.downloadUrl)}>
                      Download
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {previewingAttachment && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(15, 23, 42, 0.6)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '1.5rem',
          zIndex: 1000,
        }}>
          <div style={{
            width: 'min(960px, 100%)',
            maxHeight: '90vh',
            background: 'white',
            borderRadius: '14px',
            overflow: 'hidden',
            boxShadow: '0 24px 60px rgba(15, 23, 42, 0.2)',
            display: 'flex',
            flexDirection: 'column',
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '1rem 1.25rem',
              borderBottom: '1px solid var(--border)',
              gap: '1rem',
            }}>
              <div style={{ minWidth: 0 }}>
                <div style={{ fontWeight: 700, wordBreak: 'break-word' }}>{previewingAttachment.originalFilename}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{previewingAttachment.mimeType}</div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button className="btn btn-secondary btn-sm" onClick={() => downloadFileAuthenticated(previewingAttachment.downloadUrl)}>
                  Download
                </button>
                <button className="btn btn-secondary btn-sm" onClick={closePreview}>
                  Close
                </button>
              </div>
            </div>

            <div style={{ padding: '1rem', background: '#F8FAFC', minHeight: '320px', overflow: 'auto' }}>
              {previewLoading ? (
                <div className="loading"><div className="spinner" />Loading preview...</div>
              ) : previewError ? (
                <div className="alert alert-error">{previewError}</div>
              ) : previewBlobUrl && previewingAttachment.mimeType?.startsWith(IMAGE_MIME_PREFIX) ? (
                <img src={previewBlobUrl} alt={previewingAttachment.originalFilename} style={{ maxWidth: '100%', maxHeight: '70vh', objectFit: 'contain', display: 'block', margin: '0 auto' }} />
              ) : previewBlobUrl && previewingAttachment.mimeType === 'application/pdf' ? (
                <iframe title={previewingAttachment.originalFilename} src={previewBlobUrl} style={{ width: '100%', height: '70vh', border: 'none', background: 'white' }} />
              ) : (
                <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '3rem 0' }}>
                  Preview unavailable. Use download instead.
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

function formatFileSize(size) {
  if (!size) {
    return '';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}
