import * as fs from "node:fs";
import * as path from "node:path";
import * as vscode from "vscode";

export function activate(context: vscode.ExtensionContext): void {
  context.subscriptions.push(
    vscode.commands.registerCommand("hermes.runDesktop", runDesktop),
    vscode.commands.registerCommand("hermes.openInspector", () =>
      openInspector(context),
    ),
  );
}

function runDesktop(): void {
  const folder = vscode.workspace.workspaceFolders?.[0];
  if (!folder) {
    void vscode.window.showWarningMessage("Open a Hermes project folder first.");
    return;
  }
  const gradlew = process.platform === "win32" ? ".\\gradlew.bat" : "./gradlew";
  const terminal = vscode.window.createTerminal({
    name: "Hermes Desktop",
    cwd: folder.uri.fsPath,
  });
  terminal.show();
  terminal.sendText(`${gradlew} :game:hermesRunDesktop`);
}

function openInspector(context: vscode.ExtensionContext): void {
  const mediaRoot = vscode.Uri.joinPath(context.extensionUri, "media");
  const indexPath = path.join(mediaRoot.fsPath, "index.html");
  let html: string;
  try {
    html = fs.readFileSync(indexPath, "utf8");
  } catch {
    void vscode.window.showErrorMessage(
      "Hermes Studio UI not found in extension media/. Rebuild hermes-studio-ui and copy dist/.",
    );
    return;
  }

  const panel = vscode.window.createWebviewPanel(
    "hermesInspector",
    "Hermes Inspector",
    vscode.ViewColumn.One,
    {
      enableScripts: true,
      retainContextWhenHidden: true,
      localResourceRoots: [mediaRoot],
    },
  );

  const scriptMatch = html.match(/src="(\.\/assets\/[^"]+)"/);
  if (scriptMatch) {
    const assetRel = scriptMatch[1].replace(/^\.\//, "");
    const assetUri = vscode.Uri.joinPath(mediaRoot, assetRel);
    const webviewUri = panel.webview.asWebviewUri(assetUri);
    html = html.replace(scriptMatch[0], `src="${webviewUri}"`);
  }

  panel.webview.html = html;
}
