terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.19.0"
    }
  }

  required_version = ">= 1.2.0"
}

resource "aws_instance" "app_server" {
  ami                         = "ami-027d95b1c717e8c5d"
  instance_type               = "t2.micro"
  key_name                    = "power-app"
  associate_public_ip_address = true

  connection {
    type        = "ssh"
    user        = "ec2-user"
    private_key = file("C:\\Users\\wbroa\\Desktop\\power-app.pem")
    host        = self.public_ip
  }

  provisioner "file" {
    source      = "../target/StravaApp-1.0-SNAPSHOT-jar-with-dependencies.jar"
    destination = "/home/ec2-user/StravaApp-1.0-SNAPSHOT-jar-with-dependencies.jar"
  }

  provisioner "file" {
    source      = "../config.properties"
    destination = "/home/ec2-user/config.properties"
  }


  #TODO: Switch to using a service instead?
  provisioner "remote-exec" {
    inline = [
      "sudo sudo yum install java-17-amazon-corretto-headless -y",
      "chmod +x /home/ec2-user/StravaApp-1.0-SNAPSHOT-jar-with-dependencies.jar",
      "nohup sudo java -jar /home/ec2-user/StravaApp-1.0-SNAPSHOT-jar-with-dependencies.jar > out.log 2>&1 &",
      "sleep 1" # Needed or else the program exits - https://github.com/hashicorp/terraform/issues/6229
    ]
  }

  tags = {
    Name = "StravaPowerPRApp"
  }
}

output "public-ip"{
  value= aws_instance.app_server.public_ip
}
