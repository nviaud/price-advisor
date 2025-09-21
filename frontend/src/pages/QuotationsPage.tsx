import React, { useState } from 'react';

const QuotationsPage: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadStatus, setUploadStatus] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      setSelectedFile(event.target.files[0]);
    } else {
      setSelectedFile(null);
    }
    setUploadStatus(null);
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedFile) return;
    setIsSubmitting(true);
    setUploadStatus(null);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      const response = await fetch('http://localhost:8081/quotations', {
        method: 'POST',
        body: formData,
      });
      if (response.ok) {
        setUploadStatus('File uploaded successfully.');
        setSelectedFile(null);
      } else {
        setUploadStatus('Failed to upload file.');
      }
    } catch (error) {
      setUploadStatus('An error occurred during upload.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <h2>Quotations</h2>
      <p>This is the Quotations page.</p>
      <form onSubmit={handleSubmit}>
        <label htmlFor="quotation-upload">Upload Quotation File:</label>
        <input
          type="file"
          id="quotation-upload"
          name="quotation-upload"
          accept=".pdf,.doc,.docx,.xls,.xlsx,.csv"
          onChange={handleFileChange}
        />
        {selectedFile && (
          <div>Selected file: {selectedFile.name}</div>
        )}
        <button type="submit" disabled={!selectedFile || isSubmitting} className="ml-2">
          {isSubmitting ? 'Uploading...' : 'Submit'}
        </button>
        {uploadStatus && <div className="mt-2">{uploadStatus}</div>}
      </form>
    </div>
  );
};

export default QuotationsPage;
