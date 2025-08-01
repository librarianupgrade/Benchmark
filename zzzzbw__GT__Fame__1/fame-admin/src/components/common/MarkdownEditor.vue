<template>
  <div :id="id" />
</template>

<script>
import Editor from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all.js'

const defaultOptions = {
  minHeight: '300px',
  previewStyle: 'vertical',
  useCommandShortcut: true,
  useDefaultHTMLSanitizer: true,
  usageStatistics: false,
  hideModeSwitch: true,
}
export default {
  name: 'MarkdownEditor',
  props: {
    value: {
      type: String,
      default: '',
    },
    id: {
      type: String,
      required: false,
      default() {
        return (
          'markdown-editor-' +
          +new Date() +
          ((Math.random() * 1000).toFixed(0) + '')
        )
      },
    },
    options: {
      type: Object,
      default() {
        return defaultOptions
      },
    },
    mode: {
      type: String,
      default: 'markdown',
    },
    height: {
      type: String,
      required: false,
      default: '300px',
    },
    language: {
      type: String,
      required: false,
      default: 'en_US', // https://github.com/nhnent/tui.editor/tree/master/src/js/langs
    },
  },
  data() {
    return {
      editor: null,
    }
  },
  computed: {
    editorOptions() {
      const options = Object.assign({}, defaultOptions, this.options)
      options.initialEditType = this.mode
      options.height = this.height
      options.language = this.language
      options.plugins = [[codeSyntaxHighlight, { highlighter: Prism }]]
      return options
    },
  },
  watch: {
    value(newValue, preValue) {
      if (newValue !== preValue && newValue !== this.editor.getMarkdown()) {
        this.editor.setMarkdown(newValue)
      }
    },
    language(val) {
      this.destroyEditor()
      this.initEditor()
    },
    height(newValue) {
      this.editor.height(newValue)
    },
    mode(newValue) {
      this.editor.changeMode(newValue)
    },
  },
  mounted() {
    this.initEditor()
  },
  destroyed() {
    this.destroyEditor()
  },
  methods: {
    initEditor() {
      console.log(this.editorOptions)
      this.editor = new Editor({
        el: document.getElementById(this.id),
        ...this.editorOptions,
      })
      console.log('initEditor2')
      if (this.value) {
        this.editor.setMarkdown(this.value)
      }
      this.editor.on('change', () => {
        this.$emit('input', this.editor.getMarkdown())
      })
      console.log(this.editor)
    },
    destroyEditor() {
      if (!this.editor) return
      this.editor.off('change')
      this.editor.remove()
    },
    setValue(value) {
      this.editor.setMarkdown(value)
    },
    getValue() {
      return this.editor.getMarkdown()
    },
    setHtml(value) {
      this.editor.setHtml(value)
    },
    getHtml() {
      return this.editor.getHtml()
    },
  },
}
</script>
