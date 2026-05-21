"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.activate = activate;
const fs = __importStar(require("node:fs"));
const path = __importStar(require("node:path"));
const vscode = __importStar(require("vscode"));
function activate(context) {
    context.subscriptions.push(vscode.commands.registerCommand("hermes.runDesktop", runDesktop), vscode.commands.registerCommand("hermes.openInspector", () => openInspector(context)));
}
function runDesktop() {
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
function openInspector(context) {
    const mediaRoot = vscode.Uri.joinPath(context.extensionUri, "media");
    const indexPath = path.join(mediaRoot.fsPath, "index.html");
    let html;
    try {
        html = fs.readFileSync(indexPath, "utf8");
    }
    catch {
        void vscode.window.showErrorMessage("Hermes Studio UI not found in extension media/. Rebuild hermes-studio-ui and copy dist/.");
        return;
    }
    const panel = vscode.window.createWebviewPanel("hermesInspector", "Hermes Inspector", vscode.ViewColumn.One, {
        enableScripts: true,
        retainContextWhenHidden: true,
        localResourceRoots: [mediaRoot],
    });
    const scriptMatch = html.match(/src="(\.\/assets\/[^"]+)"/);
    if (scriptMatch) {
        const assetRel = scriptMatch[1].replace(/^\.\//, "");
        const assetUri = vscode.Uri.joinPath(mediaRoot, assetRel);
        const webviewUri = panel.webview.asWebviewUri(assetUri);
        html = html.replace(scriptMatch[0], `src="${webviewUri}"`);
    }
    panel.webview.html = html;
}
//# sourceMappingURL=extension.js.map