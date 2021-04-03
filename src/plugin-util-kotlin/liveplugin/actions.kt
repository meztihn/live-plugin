package liveplugin.actions

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.KeymapManager
import liveplugin.disposable.whenDisposed
import liveplugin.noNeedForEdtOrWriteActionWhenUsingActionManager
import liveplugin.pluginrunner.kotlin.LivePluginScript
import javax.swing.KeyStroke

/**
 * Registers action with the specified [id] (which must be unique)
 * where the action is represented as a [function] that takes an [AnActionEvent] and creates a side-effect.
 *
 * You can specify [keyStroke] for the action, for example "ctrl alt shift H" or comma-separated "alt C, alt H"
 * for shortcut with two keystrokes. Note that letters are uppercase and modification keys are lowercase.
 * For shortcuts syntax details, see [javax.swing.KeyStroke.getKeyStroke] javadoc.
 *
 * You can also specify [actionGroupId] to add actions to existing menus,
 * e.g. "ToolsMenu" corresponds to `Main menu - Tools`.
 *
 * Note that the action is registered with the `pluginDisposable` and will be automatically unregistered
 * when the plugin is unloaded or evaluated again.
 */
fun LivePluginScript.registerAction(
    id: String,
    keyStroke: String = "",
    actionGroupId: String? = null,
    function: (AnActionEvent) -> Unit
): AnAction = noNeedForEdtOrWriteActionWhenUsingActionManager {
    registerAction(id, keyStroke, actionGroupId, pluginDisposable, function.toAction())
}

fun LivePluginScript.registerAction(
    id: String,
    keyStroke: String = "",
    actionGroupId: String? = null,
    action: AnAction
): AnAction = noNeedForEdtOrWriteActionWhenUsingActionManager {
    registerAction(id, keyStroke, actionGroupId, pluginDisposable, action)
}

fun registerAction(
    id: String,
    keyStroke: String = "",
    actionGroupId: String? = null,
    disposable: Disposable,
    function: (AnActionEvent) -> Unit
): AnAction = noNeedForEdtOrWriteActionWhenUsingActionManager {
    registerAction(id, keyStroke, actionGroupId, disposable, function.toAction())
}

fun registerAction(
    id: String,
    keyStroke: String = "",
    actionGroupId: String? = null,
    disposable: Disposable,
    action: AnAction
): AnAction = noNeedForEdtOrWriteActionWhenUsingActionManager {
    val actionManager = ActionManager.getInstance()
    val keymapManager = KeymapManager.getInstance()

    if ((actionManager.getAction(id) != null)) error("Action '$id' is already registered")
    action.templatePresentation.setText(id, true)

    val actionGroup = if (actionGroupId == null) null else actionManager.getAction(actionGroupId) as? DefaultActionGroup
    val shortcut = keyStroke.toKeyboardShortcut()

    if (shortcut != null) keymapManager.activeKeymap.addShortcut(id, shortcut)
    actionManager.registerAction(id, action)
    actionGroup?.add(action)

    disposable.whenDisposed {
        actionGroup?.remove(action)
        actionManager.unregisterAction(id)
        if (shortcut != null) keymapManager.activeKeymap.removeShortcut(id, shortcut)
    }
    return action
}

private fun String.toKeyboardShortcut(): KeyboardShortcut? {
    val parts = trim().split(",")
    if (parts.isEmpty()) return null

    val firstKeystroke = KeyStroke.getKeyStroke(parts[0]) ?: error("Invalid keystroke '${this}'")
    val secondKeystroke = if (parts.size > 1) KeyStroke.getKeyStroke(parts[1]) else null
    return KeyboardShortcut(firstKeystroke, secondKeystroke)
}

private fun ((AnActionEvent) -> Unit).toAction() =
    object: AnAction() {
        override fun actionPerformed(event: AnActionEvent) = this@toAction(event)
        override fun isDumbAware() = true
    }
