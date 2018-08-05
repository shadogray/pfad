//import Quill from 'quill'

let AlignStyle = Quill.import('attributors/style/align')
let BackgroundStyle = Quill.import('attributors/style/background')
let ColorStyle = Quill.import('attributors/style/color')
let DirectionStyle = Quill.import('attributors/style/direction')
let FontStyle = Quill.import('attributors/style/font')
let SizeStyle = Quill.import('attributors/style/size')    

Quill.register(AlignStyle, true);
Quill.register(BackgroundStyle, true);
Quill.register(ColorStyle, true);
Quill.register(DirectionStyle, true);
Quill.register(FontStyle, true);
Quill.register(SizeStyle, true);

const Parchment = Quill.import('parchment')

class IndentAttributor extends Parchment.Attributor.Style {
  add (node, value) {
    if (value === 0) {
      this.remove(node)
      return true
    } else {
      return super.add(node, `${value}em`)
    }
  }
}

let IndentStyle = new IndentAttributor('indent', 'text-indent', {
  scope: Parchment.Scope.BLOCK,
  whitelist: ['1em', '2em', '3em', '4em', '5em', '6em', '7em', '8em', '9em']
})

//export { IndentStyle }

Quill.register(IndentStyle, true)