document.addEventListener('DOMContentLoaded', () => {
    const selectedTextArea = document.getElementById('selected-text');
    const summarizeBtn = document.getElementById('summarize-btn');
    const suggestBtn = document.getElementById('suggest-btn');
    const responseDiv = document.getElementById('response');
  
    // Get selected text from the active tab
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      chrome.scripting.executeScript(
        {
          target: { tabId: tabs[0].id },
          func: () => window.getSelection().toString(),
        },
        (results) => {
          if (results && results[0] && results[0].result) {
            selectedTextArea.value = results[0].result;
          }
        }
      );
    });
  
    // Function to send data to the backend
    const sendToBackend = async (operation) => {
      const content = selectedTextArea.value.trim();
      if (!content) {
        responseDiv.textContent = 'Please select some text first.';
        return;
      }
  
      const payload = {
        content,
        operation,
      };
  
      try {
        responseDiv.textContent = 'Processing...';
        const response = await fetch('http://localhost:8080/api/research/process', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });
  
        const data = await response.text();
        responseDiv.textContent = data;
      } catch (error) {
        responseDiv.textContent = 'Error: ' + error.message;
      }
    };
  
    // Button event listeners
    summarizeBtn.addEventListener('click', () => sendToBackend('summarize'));
    suggestBtn.addEventListener('click', () => sendToBackend('suggest'));
  });