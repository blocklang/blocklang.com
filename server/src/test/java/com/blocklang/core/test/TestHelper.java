package com.blocklang.core.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.lang.Nullable;

public class TestHelper {

	// FIXME：因为 5.5.2 中删除临时文件夹有 bug，所以手动删，当 junit 修复此 bug 后，删除此代码。
	public static void clearDir(Path folder) throws IOException {
		if(Files.exists(folder)) {
			deleteRecursively(folder);
		}
	}
		
	private static boolean deleteRecursively(@Nullable Path root) throws IOException {
		if (root == null) {
			return false;
		}
		if (!Files.exists(root)) {
			return false;
		}

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File beFile = file.toFile();
				if(!beFile.canWrite()) {
					// 关键是设置可写权限
					beFile.setWritable(true);
				}
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		return true;
	}
}
