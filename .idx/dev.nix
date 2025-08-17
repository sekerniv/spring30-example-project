{ pkgs, ... }: {
  channel = "stable-23.11";
  packages = [
    pkgs.zulu17
    pkgs.maven
  ];
  env = { };

  idx = {
    extensions = [
      "vscjava.vscode-java-pack"
    ];
    workspace = {
      onCreate = {
        install = "mvn clean install";
      };
      onStart = {
        run-server = "PORT=8080 mvn spring-boot:run";
      };
    };
  };
}
