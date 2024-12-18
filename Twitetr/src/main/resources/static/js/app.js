// // AMJAD: Strukturera designen av datavisning för stavningsförslag och interaktivitet med JavaScript.
document.addEventListener("DOMContentLoaded", function () {
    // Initialize Quill with existing toolbar options
    const quill = new Quill('#editor', {
        theme: 'snow',
        placeholder: 'Write your post here...',
        modules: {
            toolbar: [
                [{ header: [1, 2, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                ['link', 'image'],
                [{ list: 'ordered' }, { list: 'bullet' }],
                ['clean'],
            ],
        },
    });

    // DOM Elements
    const charCounter = document.getElementById('char-counter');
    const submitButton = document.getElementById('submit-button');
    const loader = document.getElementById('loader');
    const previewContainer = document.getElementById('preview-container');
    const tweetPreview = document.getElementById('tweet-preview');
    const suggestionContainer = document.getElementById('suggestion-container');

    // Spell-Check Variables
    let currentRange;
    let currentWord = '';

    // Sample Dictionary for Demonstration
    // Replace this with a real dictionary or API call for production
    const dictionary = {
        'teh': ['the', 'tech', 'them'],
        'recieve': ['receive'],
        'adress': ['address'],
        'definately': ['definitely'],
        'occured': ['occurred'],
        'seperate': ['separate'],
        // Add more misspelled words and suggestions as needed
    };

    // Function to Get Suggestions for a Given Word
    function getSuggestionsFor(word) {
        const lowerWord = word.toLowerCase();
        return dictionary[lowerWord] || [];
    }

    // Function to Show Suggestions
    function showSuggestions(suggestions, coords) {
        if (suggestions.length === 0) {
            hideSuggestions();
            return;
        }

        // Clear Previous Suggestions
        suggestionContainer.innerHTML = '';

        // Populate Suggestions
        suggestions.forEach(suggestion => {
            const p = document.createElement('p');
            p.textContent = suggestion;
            p.addEventListener('click', function () {
                replaceCurrentWord(suggestion);
                hideSuggestions();
            });
            suggestionContainer.appendChild(p);
        });

        // Position the Suggestion Container
        suggestionContainer.style.top = `${coords.top}px`;
        suggestionContainer.style.left = `${coords.left}px`;
        suggestionContainer.classList.remove('hidden');
    }

    // Function to Hide Suggestions
    function hideSuggestions() {
        suggestionContainer.classList.add('hidden');
    }

    // Function to Replace the Current Word with the Selected Suggestion
    function replaceCurrentWord(suggestion) {
        if (!currentRange) return;

        const pos = currentRange.index;
        const editorText = quill.getText();
        let start = pos - 1;

        // Find the Start of the Current Word
        while (start >= 0 && !/\s/.test(editorText.charAt(start))) {
            start--;
        }
        start++;

        // Find the End of the Current Word
        let end = pos - 1;
        while (end < editorText.length && !/\s/.test(editorText.charAt(end))) {
            end++;
        }

        // Replace the Misspelled Word with the Suggestion
        quill.deleteText(start, end - start);
        quill.insertText(start, suggestion, 'user');

        // Move Cursor to After the Inserted Suggestion
        quill.setSelection(start + suggestion.length, 0);
    }

    // Function to Update Character Count and Enable/Disable Submit Button
    function updateCharacterCount() {
        const text = quill.getText().trim();
        charCounter.textContent = `${text.length} / 280`;
        submitButton.disabled = text.length === 0 || text.length > 280;
    }

    // Function to Update the Tweet Preview (Optional)
    function updatePreview() {
        tweetPreview.innerHTML = quill.root.innerHTML;
        previewContainer.classList.remove('hidden');
    }

    // Event Listener for Text Changes in Quill Editor
    quill.on('text-change', function (delta, oldDelta, source) {
        if (source !== 'user') return;

        updateCharacterCount();
        updatePreview();

        // Get Current Selection
        currentRange = quill.getSelection();
        if (!currentRange) {
            hideSuggestions();
            return;
        }

        // Get Text Up to the Current Cursor Position
        const textUpToCursor = quill.getText(0, currentRange.index);
        const words = textUpToCursor.split(/\s+/);
        currentWord = words[words.length - 1] || '';

        // Check if the Last Character is a Space (Word Completed)
        if (/\s$/.test(textUpToCursor)) {
            const suggestions = getSuggestionsFor(currentWord);
            if (suggestions.length > 0) {
                // Get the Position to Display Suggestions
                const bounds = quill.getBounds(currentRange.index);
                const editorContainer = document.getElementById('editor-container');
                const rect = editorContainer.getBoundingClientRect();
                const top = rect.top + window.scrollY + bounds.bottom + 5; 
                const left = rect.left + window.scrollX + bounds.left;

                showSuggestions(suggestions, { top, left });
            } else {
                hideSuggestions();
            }
        } else {
            hideSuggestions();
        }
    });

    // Event Listener for Form Submission
    submitButton.addEventListener('click', async (e) => {
        e.preventDefault(); 

        const tweetContent = quill.getContents(); 
        const plainText = quill.getText().trim(); 

        if (plainText.length === 0 || plainText.length > 280) {
            alert('Tweet must be between 1 and 280 characters.');
            return;
        }

        try {
            // Show Loader
            loader.classList.remove('hidden');

            // Send Tweet as JSON to Backend
            const response = await fetch('/api/tweet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: plainText }),
            });

            if (response.ok) {
                const result = await response.json();
                alert(`Tweet saved: ${result.message}`);
                quill.setText('');
                updateCharacterCount();
                hideSuggestions(); 
                previewContainer.classList.add('hidden'); 
            } else {
                alert('Failed to post the tweet. Try again!');
            }
        } catch (error) {
            console.error('Error posting tweet:', error);
            alert('Something went wrong. Please try again!');
        } finally {
            // Hide Loader
            loader.classList.add('hidden');
        }
    });

    // Optional: Hide Suggestions When Clicking Outside
    document.addEventListener('click', function (event) {
        if (!suggestionContainer.contains(event.target) && event.target !== quill.root) {
            hideSuggestions();
        }
    });
});