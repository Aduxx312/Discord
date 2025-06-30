🏆 Aduxx Discord Reward Plugin
Discord Reward is a  Minecraft plugin designed for servers that want to reward players for joining or verifying with their Discord community. Simple, fast, and fully customizable.

✨ Features
✅ GUI-based reward system

🔗 Reward players who verify via Discord bot

🧍 Per-player reward tracking (using UUIDs)

📁 Simple YAML configuration file

🔒 Prevents multiple claims

⚙️ Designed for PaperMC 1.21+

🪶 Minimal performance impact

🧑‍💻 How It Works
Players use /reward  in-game.

A clean GUI shows if the reward is claimed:

✅ Green icon if claimed

❌ Red icon if not

Discord bot marks the player as "verified" via UUID.

Plugin checks config and gives rewards accordingly.

📦 Setup
Drop the .jar into your plugins/ folder.

Restart your server.

Configure your config.yml and connect it with your Discord bot or external system that marks verified players by UUID.

Done!

💡 Integration Idea
Use your Discord bot (or web app) to write to the plugin's config file, adding the player's UUID once they verify. This makes the process seamless and secure.
