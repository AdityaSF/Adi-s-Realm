package adiitya.adisrealm.cmd;

import adiitya.adisrealm.utils.DataManager;
import adiitya.adisrealm.utils.MinecraftUtils;
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class NicknameCommand implements ICommand {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length > 0) {

			String arg = args[0];

			if (arg.equalsIgnoreCase("list"))
				list(sender, args);
			else if (arg.equalsIgnoreCase("add"))
				addNickname(sender, args);
			else if (arg.equalsIgnoreCase("remove"))
				removeNickname(sender, args);
			else
				sender.sendMessage("§cUSAGE: /" + label + " <add | remove| list> ...");
		} else {
			sender.sendMessage("§cUSAGE: /" + label + " <add | remove | list> ...");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		List<String> matches = Lists.newArrayList();

		if (args.length < 1)
			matches.addAll(Arrays.asList("add", "remove", "list"));
		else if (args.length == 1) {

			String search = args[0];

			if (search.equalsIgnoreCase("remove")) matches.addAll(DataManager.getNicknames(((Player) sender).getUniqueId()));
		}

		return matches;
	}

	@Override
	public String getName() {
		return "nickname";
	}

	private void addNickname(CommandSender sender, String[] args) {

		if (args.length > 1) {
			if (sender instanceof Player) {

				Player p = (Player) sender;
				String nick = args[1];
				int status = DataManager.addNickname(p.getUniqueId(), nick);

				if (status == 0) // success
					sender.sendMessage("§9Successfully nicknamed you §6" + nick);
				else if (status == 1) // taken
					sender.sendMessage("§9The nickname §6" + nick + " §9is taken");
				else if (status == 2) // duplicate (user already has nick)
					sender.sendMessage("§9You already have the nickname §6" + nick);
				else if (status == 3) // username
					sender.sendMessage("§9The nickname §6" + nick + " §9is somebodies username");
				else if (status == 5) // bad length
					sender.sendMessage("§9The nickname §6" + nick + " §9is too long or short");
				else // error
					sender.sendMessage("§9An unknown error occurred and your nickname hasn't been added");
			} else {
				sender.sendMessage("§cYou must be a player to use that!");
			}
		} else {
			sender.sendMessage("§cUSAGE: /nickname add <nickname>");
		}
	}

	private void removeNickname(CommandSender sender, String[] args) {

		if (args.length > 1) {
			if (sender instanceof Player) {
				DataManager.removeNickname(((Player) sender).getUniqueId(), args[1]);
				sender.sendMessage("§9Removed nickname §c" + args[1]);
			} else {
				sender.sendMessage("§cYou must be a player to use that!");
			}
		} else {
			sender.sendMessage("§cUSAGE: /nickname remove <nickname>");
		}
	}

	private void list(CommandSender sender, String[] args) {

		if (args.length > 1)
			listFromUsername(sender, args[1]);
		else if (sender instanceof Player)
			listFromUsername(sender, sender.getName());
		else
			sender.sendMessage("§cYou must be a player to use that!");
	}

	private void listFromUsername(CommandSender sender, String name) {

		Optional<UUID> uuid = getUuid(name);

		if (uuid.isPresent()) {
			listFromUuid(sender, uuid.get());
		} else {
			sender.sendMessage("§cPlayer not found");
		}
	}

	private void listFromUuid(CommandSender sender, UUID uuid) {

		boolean targetExists = MinecraftUtils.playerExists(uuid);

		if (targetExists) {

			String username = MinecraftUtils.getUsername(uuid).orElse("");

			List<String> nicknames = DataManager.getNicknames(uuid).stream()
					.filter(n -> !n.equalsIgnoreCase(username))
					.collect(Collectors.toList());

			if (nicknames.isEmpty()) {
				sender.sendMessage(username + " §9has no nicknames");
			} else {
				sender.sendMessage("§9Nicknames for §r" + username);
				nicknames.forEach(nick -> sender.sendMessage("§c> §9" + nick));
			}
		} else {
			sender.sendMessage("§cPlayer not found");
		}
	}

	private Optional<UUID> getUuid(String name) {

		Optional<UUID> nickUUID = DataManager.getUserFromNickname(name);

		return nickUUID.isPresent() ? nickUUID : MinecraftUtils.getUuid(name);
	}
}
