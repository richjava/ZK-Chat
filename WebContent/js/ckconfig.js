CKEDITOR.editorConfig = function(config) {
	config.toolbar = 'MyToolbar';

	config.toolbar_MyToolbar = [ [ 'Preview' ],
			[ 'Cut', 'Copy', 'Paste', 'PasteFromWord', '-' ],
			[ 'Undo', 'Redo' ], [ 'Smiley' ], [ 'Bold', 'Italic' ], [ 'Link' ] ];
};