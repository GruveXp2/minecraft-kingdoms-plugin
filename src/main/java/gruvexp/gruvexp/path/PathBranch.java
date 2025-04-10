package gruvexp.gruvexp.path;

import java.util.Set;

public record PathBranch(Path path, int enterIndex, Set<String> addresses) {
}
