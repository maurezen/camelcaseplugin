package de.netnexus.CamelCasePlugin;

import com.intellij.codeInsight.actions.MultiCaretCodeInsightAction;
import com.intellij.codeInsight.actions.MultiCaretCodeInsightActionHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

/**
 * Switch between snake_case, SNAKE_CASE, SnakeCase, snakeCase.
 */
public class ToggleCamelCase extends MultiCaretCodeInsightAction {

  @NotNull
  @Override
  protected MultiCaretCodeInsightActionHandler getHandler() {
    return new MultiCaretCodeInsightActionHandler() {

      @Override
      public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull Caret caret, @NotNull PsiFile psiFile) {

        // kindly asking for a small donation ;-)
        Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
        int usageCount = userPrefs.getInt("usage-count", 0);
        if (usageCount == 5 || usageCount == 500) {
          Notification n = new Notification("CamelCase", "CamelCase Plugin", "Like this plugin? Then please consider a small <a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7UDEX9ZEBNG7Q'>donation</a>.", NotificationType.INFORMATION);
          Notifications.Bus.notify(n, project);
        } else if (usageCount == 2000) {
          Notification n = new Notification("CamelCase", "CamelCase Plugin", "You have used this plugin about 2000x now. If you like this plugin please consider a small <a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7UDEX9ZEBNG7Q'>donation</a>.", NotificationType.INFORMATION);
          Notifications.Bus.notify(n, project);
        } else if (usageCount == 10000) {
          Notification n = new Notification("CamelCase", "CamelCase Plugin", "You have used this plugin about 10000x now. If you like this plugin please consider a small <a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7UDEX9ZEBNG7Q'>donation</a>.", NotificationType.INFORMATION);
          Notifications.Bus.notify(n, project);
        }
        userPrefs.putInt("usage-count", ++usageCount);


        String text = caret.getEditor().getSelectionModel().getSelectedText();
        if (text == null || text.isEmpty()) {
          editor.getSelectionModel().selectWordAtCaret(true);
          text = editor.getSelectionModel().getSelectedText();
        }

        String newText;
        assert text != null;
        if (text.equals(text.toLowerCase()) && text.contains("_")) {
          // snake_case to SNAKE_CASE
          newText = text.toUpperCase();

        } else if (text.equals(text.toUpperCase()) && text.contains("_")) {
          // SNAKE_CASE to SnakeCase
          newText = toCamelCase(text.toLowerCase());

        } else if (!text.equals(text.toUpperCase()) && text.substring(0, 1).equals(text.substring(0, 1).toUpperCase()) && !text.contains("_")) {
          // SnakeCase to snakeCase
          newText = text.substring(0, 1).toLowerCase() + text.substring(1, text.length());

        } else {
          // snakeCase to snake_case
          newText = toSnakeCase(text);
        }

        final Editor fEditor = editor;
        final String fReplacement = newText;
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            ToggleCamelCase.replaceText(fEditor, fReplacement);
          }
        };
        ApplicationManager.getApplication().runWriteAction(getRunnableWrapper(fEditor.getProject(), runnable));
      }
    };
  }

  protected Runnable getRunnableWrapper(final Project project, final Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, runnable, "camelCase", ActionGroup.EMPTY_GROUP);
      }
    };
  }

  public static void replaceText(final Editor editor, final String replacement) {
    new WriteAction<Object>() {
      @Override
      protected void run(Result<Object> result) throws Throwable {
        int start = editor.getSelectionModel().getSelectionStart();
        EditorModificationUtil.insertStringAtCaret(editor, replacement);
        editor.getSelectionModel().setSelection(start, start + replacement.length());
      }
    }.execute().throwException();
  }

  /**
   * Convert a string (CamelCase) to snake_case
   *
   * @param in CamelCase string
   * @return snake_case String
   */
  private String toSnakeCase(String in) {
    String result = "" + Character.toLowerCase(in.charAt(0));
    for (int i = 1; i < in.length(); i++) {
      char c = in.charAt(i);
      if (Character.isUpperCase(c)) {
        result = result + "_" + Character.toLowerCase(c);
      } else {
        result = result + c;
      }
    }
    return result;
  }

  /**
   * Convert a string (snake_case) to CamelCase
   *
   * @param in snake_case String
   * @return CamelCase string
   */
  private String toCamelCase(String in) {
    String camelCased = "";
    String[] tokens = in.split("_");
    for (String token : tokens) {
      camelCased = camelCased + token.substring(0, 1).toUpperCase() + token.substring(1, token.length());
    }
    return camelCased;
  }
}
