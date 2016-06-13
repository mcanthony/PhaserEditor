// The MIT License (MIT)
//
// Copyright (c) 2015, 2016 Arian Fornaris
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions: The above copyright notice and this permission
// notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.
package phasereditor.canvas.core;

import static java.lang.String.format;

import phasereditor.assetpack.core.IAssetFrameModel;
import phasereditor.assetpack.core.IAssetKey;
import phasereditor.assetpack.core.ImageAssetModel;
import phasereditor.assetpack.core.SpritesheetAssetModel;
import phasereditor.lic.LicCore;

/**
 * @author arian
 *
 */
public class JSCodeGenerator implements ICodeGenerator {

	@Override
	public String generate(WorldModel model) {
		StringBuilder sb = new StringBuilder();
		sb.append("// Generated by " + LicCore.PRODUCT_NAME + "\n\n");
		String classname = model.getClassName();
		sb.append("function " + classname + "(game, parent) {\n");

		generateGroup(0, sb, model);

		sb.append("\n");
		sb.append(tabs(1) + "// call the 'userInit' method if exists \n");
		sb.append(tabs(1) + "if (this.userInit) {\n");
		sb.append(tabs(2) + "this.userInit();\n");
		sb.append(tabs(1) + "}\n");

		sb.append("}\n");

		sb.append(classname + ".prototype = Object.create(Phaser.Group);\n");
		sb.append(classname + ".prototype.constructor = Phaser.Group;\n");
		sb.append("\n");
		sb.append("// --- end generated code ---");
		return sb.toString();
	}

	private static void generate(int indent, StringBuilder sb, BaseObjectModel model) {
		if (model instanceof GroupModel) {
			generateGroup(indent, sb, (GroupModel) model);
		} else if (model instanceof BaseSpriteModel) {
			generateSprite(indent, sb, (BaseSpriteModel) model);
		}
	}

	private static void generateSprite(int indent, StringBuilder sb, BaseSpriteModel model) {
		GroupModel parGroup = model.getParent();
		String parVar = parGroup.isWorldModel() ? "this" : "this." + parGroup.getEditorName();

		sb.append(tabs(indent));
		String varname = model.getEditorName();
		sb.append("this." + varname + " = game.add.");
		if (model instanceof ImageSpriteModel) {
			ImageSpriteModel image = (ImageSpriteModel) model;
			sb.append("sprite(" + // sprite
					round(image.getX())// x
					+ ", " + round(image.getY()) // y
					+ ", '" + image.getAssetKey().getKey() + "'" // key
					+ ", null" // frame
					+ ", " + parVar // group
					+ ")");
		} else if (model instanceof SpritesheetSpriteModel || model instanceof AtlasSpriteModel) {
			AssetSpriteModel<?> sprite = (AssetSpriteModel<?>) model;
			IAssetKey frame = sprite.getAssetKey();
			String frameValue = frame instanceof SpritesheetAssetModel.FrameModel
					? Integer.toString(((SpritesheetAssetModel.FrameModel) frame).getIndex())
					: "'" + frame.getKey() + "'";
			sb.append("sprite(" + // sprite
					round(sprite.getX())// x
					+ ", " + round(sprite.getY()) // y
					+ ", '" + sprite.getAssetKey().getAsset().getKey() + "'" // key
					+ ", " + frameValue // frame
					+ ", " + parVar // group
					+ ")");
		} else if (model instanceof ButtonSpriteModel) {
			ButtonSpriteModel button = (ButtonSpriteModel) model;
			String outFrameKey;
			if (button.getAssetKey().getAsset() instanceof ImageAssetModel) {
				// buttons based on image do not have outFrames
				outFrameKey = "null";
			} else {
				// TODO: out frame should be the same of frameName!!!!!
				if (button.getOutFrame() == null) {
					outFrameKey = frameKey((IAssetFrameModel) button.getAssetKey());
				}
			}

			outFrameKey = frameKey(button.getOutFrame());
			sb.append("button(" + // sprite
					round(button.getX())// x
					+ ", " + round(button.getY()) // y
					+ ", '" + button.getAssetKey().getAsset().getKey() + "'" // key
					+ ", '<missing callback>'" // callback
					+ ", this" // context
					+ ", " + frameKey(button.getOverFrame())// overFrame
					+ ", " + outFrameKey// outFrame
					+ ", " + frameKey(button.getDownFrame())// downFrame
					+ ", " + frameKey(button.getUpFrame())// upFrame
					+ ", " + parVar // group
					+ ")");
		} else if (model instanceof TileSpriteModel) {
			TileSpriteModel tile = (TileSpriteModel) model;
			sb.append("tileSprite(" + // sprite
					round(tile.getX())// x
					+ ", " + round(tile.getY()) // y
					+ ", " + round(tile.getWidth()) // width
					+ ", " + round(tile.getHeight()) // height
					+ ", '" + tile.getAssetKey().getAsset().getKey() + "'" // key
					+ ", '" + tile.getAssetKey().getKey() + "'"// frame
					+ ", " + parVar // group
					+ ")");
		}
		sb.append(";\n");

		generateDisplayProps(indent, sb, model);

		generateSpriteProps(indent, sb, model);
	}

	private static String round(double x) {
		return Integer.toString((int) Math.round(x));
	}

	private static void generateDisplayProps(int indent, StringBuilder sb, BaseObjectModel model) {
		String tabs = tabs(indent);
		String varname = "this." + model.getEditorName();

		if (model.getAngle() != 0) {
			sb.append(tabs + varname + ".angle = " + model.getAngle() + ";\n");
		}

		if (model.getScaleX() != 1 || model.getScaleY() != 1) {
			sb.append(tabs + varname + ".scale.set(" + model.getScaleX() + ", " + model.getScaleY() + ");\n");
		}

		if (model.getPivotX() != 0 || model.getPivotY() != 0) {
			sb.append(tabs + varname + ".pivot.set(" + model.getPivotX() + ", " + model.getPivotY() + ");\n");
		}
	}

	private static void generateSpriteProps(int indent, StringBuilder sb, BaseSpriteModel model) {
		String tabs = tabs(indent);
		String varname = "this." + model.getEditorName();

		if (model.getAnchorX() != 0 || model.getAnchorY() != 0) {
			sb.append(tabs + varname + ".anchor.set(" + model.getAnchorX() + ", " + model.getAnchorY() + ");\n");
		}

		if (model.getTint() != null && !model.getTint().equals("0xffffff")) {
			sb.append(tabs + varname + ".tint = " + model.getTint() + ";\n");
		}
	}

	private static void generateGroup(int indent, StringBuilder sb, GroupModel group) {
		String tabs = tabs(indent);

		if (!group.isWorldModel()) {
			GroupModel parGroup = group.getParent();
			String parVar = parGroup.isWorldModel() ? "this" : "this." + parGroup.getEditorName();
			sb.append(tabs);
			sb.append(format("this.%s = game.add.group(%s);\n\n", group.getEditorName(), parVar));
		}

		int i = 0;
		int last = group.getChildren().size() - 1;
		for (BaseObjectModel child : group.getChildren()) {
			generate(indent + 1, sb, child);
			if (i < last) {
				sb.append("\n");
			}
			i++;
		}

		generateDisplayProps(indent, sb, group);
	}

	private static String tabs(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	private static String frameKey(IAssetFrameModel frame) {
		return frame == null ? "null" : "'" + frame.getKey() + "'";
	}
}